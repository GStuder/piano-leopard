package net.kreatious.pianoleopard.midi.sequencer;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.State;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

import net.kreatious.pianoleopard.midi.ParsedSequence;
import net.kreatious.pianoleopard.midi.event.Event;
import net.kreatious.pianoleopard.midi.event.EventFactory;

import com.google.common.annotations.VisibleForTesting;

/**
 * Model for the MIDI output sequencer, allows controllers to listen for events.
 *
 * @author Jay-R Studer
 */
public class OutputModel implements AutoCloseable {
    /**
     * Indicates which action to take in response to an event handler.
     * <p>
     * The action with the highest priority takes precedence.
     *
     * @author Jay-R Studer
     */
    public enum EventAction {
        /**
         * Return if the event should be played. Has the highest priority.
         */
        PLAY,

        /**
         * Returned if the event should be muted. Has medium priority.
         */
        MUTE,

        /**
         * Returned if it doesn't matter if the event is muted. Has the lowest
         * priority.
         * <p>
         * An event that is not handled by all handlers will be played.
         * <p>
         * Event handlers are allowed to change the MidiMessage before it is
         * sent.
         */
        UNHANDLED;
    }

    private static final long ALWAYS_SEND = -10;
    private final Sequencer sequencer;
    private ParsedSequence sequence = ParsedSequence.createEmpty();
    private Optional<MidiDevice> output = Optional.empty();
    private Optional<Receiver> receiver = Optional.empty();

    private final List<Consumer<? super Info>> outputDeviceListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<? super ParsedSequence>> openListeners = new CopyOnWriteArrayList<>();
    private final List<Runnable> playListeners = new CopyOnWriteArrayList<>();
    private final List<LongConsumer> currentTimeListeners = new CopyOnWriteArrayList<>();
    private final List<BiFunction<MidiMessage, Optional<Event>, EventAction>> eventHandlers = new CopyOnWriteArrayList<>();
    private final List<Closeable> closeables = new CopyOnWriteArrayList<>();

    private final Thread tickThread = new Thread("output model current tick thread") {
        @Override
        public void run() {
            try {
                while (true) {
                    currentTimeListeners.forEach(listener -> listener.accept(sequencer.getMicrosecondPosition()));
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1) / 120);
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    };

    /**
     * Constructs a new {@link OutputModel} with the specified initial state.
     * <p>
     * The output model is initially unconnected to any MIDI devices. After
     * construction, it is expected that an output device will be set by the
     * consumer.
     *
     * @param sequencerFactory
     *            A factory for producing the {@link Sequencer}, such as
     *            {@link SystemSequencerFactory}.
     * @throws MidiUnavailableException
     *             if the MIDI system is unavailable.
     */
    public OutputModel(SequencerFactory sequencerFactory) throws MidiUnavailableException {
        sequencer = sequencerFactory.getSequencer();
        setOutputDevice(new InitialMidiDevice());
    }

    /**
     * Reconnects the sequencer to a different MIDI output device.
     *
     * @param output
     *            the new output MIDI device to reconnect to
     * @throws MidiUnavailableException
     *             if the MIDI system is unavailable.
     */
    public synchronized void setOutputDevice(MidiDevice output) throws MidiUnavailableException {
        try {
            sequencer.close();
            this.output.ifPresent(MidiDevice::close);
            this.output = Optional.of(output);

            output.open();
            receiver = Optional.of(new MutingReceiverProxy(output.getReceiver()));
            sequencer.getTransmitter().setReceiver(receiver.get());
            sequencer.open();
            sequencer.setSequence(sequence.getSequence());
            outputDeviceListeners.forEach(listener -> listener.accept(output.getDeviceInfo()));
        } catch (final InvalidMidiDataException e) {
            // Sequence should still be valid since openMidiFile didn't throw
            throw new IllegalStateException(e);
        }
    }

    /**
     * Starts playback of the currently loaded MIDI file.
     * <p>
     * Any registered start listeners will be called with the last opened MIDI
     * sequence.
     */
    public void start() {
        sequencer.stop();
        sequencer.setMicrosecondPosition(0);
        resetReceiver();
        playListeners.forEach(Runnable::run);
        sequencer.start();
    }

    private class MutingReceiverProxy implements Receiver {
        private final Receiver wrapped;

        private MutingReceiverProxy(Receiver wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void send(MidiMessage message, long timeStamp) {
            if (timeStamp == ALWAYS_SEND) {
                wrapped.send(message, -1);
                return;
            }

            final Optional<Event> event = EventFactory.create(message, sequencer.getMicrosecondPosition());
            if (eventHandlers.stream().map(eventHandler -> eventHandler.apply(message, event)).min(Enum::compareTo)
                    .orElse(EventAction.UNHANDLED) != EventAction.MUTE) {
                wrapped.send(message, timeStamp);
            }
        }

        @Override
        public void close() {
            wrapped.close();
        }
    }

    /**
     * Adjusts the tempo of played back sequences.
     * <p>
     * Values higher than 1.0 are faster than normal, less than 1.0 are slower
     * than normal. A value of 1.0 indicates that the regular tempo should be
     * applied. Tempo factors do not affect the microsecond values of MIDI
     * events.
     *
     * @param factor
     *            the tempo factor to set
     */
    public void setTempoFactor(float factor) {
        sequencer.setTempoFactor(factor);
    }

    /**
     * Seeks the sequence to the specified time
     *
     * @param time
     *            the time in microseconds to seek to
     */
    public void setCurrentTime(long time) {
        if (sequencer.isRunning()) {
            sequencer.stop();
            sequencer.setMicrosecondPosition(time);
            sequencer.start();
        } else {
            sequencer.setMicrosecondPosition(time);
        }
    }

    /**
     * Parses a MIDI file and prepares it for playback.
     * <p>
     * Any registered start listeners will be called with the parsed sequence.
     * The file of the parsed sequence will be the specified MIDI file.
     *
     * @param midi
     *            the MIDI file to open
     * @throws IOException
     *             if an I/O error occurs
     */
    public void openMidiFile(File midi) throws IOException {
        try (InputStream in = new FileInputStream(midi)) {
            openMidiFile(in, Optional.of(midi));
        }
    }

    @VisibleForTesting
    void openMidiFile(InputStream midiStream, Optional<File> midi) throws IOException {
        try {
            if (tickThread.getState() == State.NEW) {
                tickThread.start();
            }

            sequence = ParsedSequence.parseByTracks(MidiSystem.getSequence(midiStream));
            sequence.setFile(midi);
            sequencer.stop();
            sequencer.setSequence(sequence.getSequence());
            sequencer.setMicrosecondPosition(0);
            resetReceiver();
            openListeners.forEach(listener -> listener.accept(sequence));
        } catch (final InvalidMidiDataException e) {
            throw new IOException(e);
        }
    }

    /**
     * Adds a listener to notify when the output device has changed.
     *
     * @param listener
     *            the listener to add
     */
    public void addOutputDeviceListener(Consumer<? super Info> listener) {
        outputDeviceListeners.add(listener);
    }

    /**
     * Adds a listener to notify when a parsed MIDI file is opened.
     *
     * @param listener
     *            the listener to add
     */
    public void addOpenListener(Consumer<? super ParsedSequence> listener) {
        openListeners.add(listener);
    }

    /**
     * Adds a listener to notify when a parsed MIDI file is played from the
     * beginning.
     *
     * @param listener
     *            the listener to add
     */
    public void addPlayListener(Runnable listener) {
        playListeners.add(listener);
    }

    /**
     * Adds an event handler to handle MIDI events.
     * <p>
     * The return value of the handler determines the action to take. A list of
     * actions is provided on {@link EventAction}. The default action is to play
     * the event.
     * <p>
     * Event handlers are allowed to mutate the channel of the MidiMessage
     * object before returning. The {@link Event} object contains the original
     * message before any mutations are applied. Handlers are encouraged to use
     * the Event object whenever possible.
     *
     * @param handler
     *            the event handler to add.
     */
    public void addEventHandler(BiFunction<MidiMessage, Optional<Event>, EventAction> handler) {
        eventHandlers.add(handler);
    }

    /**
     * Adds a listener to notify when the current playback time in microseconds
     * has changed.
     * <p>
     * This listener is called asynchronously several times per second for the
     * lifetime of this object from a different thread than the one which
     * invokes this method.
     *
     * @param listener
     *            the listener to add
     */
    public void addCurrentTimeListener(LongConsumer listener) {
        currentTimeListeners.add(listener);
    }

    /**
     * Adds a closeable to close when this output model is closed.
     * <p>
     * Used for releasing resources closely tied with the lifetime of this
     * output model. Resources will be released in the same order they are
     * registered.
     *
     * @param closeable
     *            the closeable to add
     */
    public void addCloseable(Closeable closeable) {
        closeables.add(closeable);
    }

    /**
     * Sends a MIDI message to the output.
     *
     * @param message
     *            the MIDI message to send to the connected output device
     */
    public synchronized void sendMessage(MidiMessage message) {
        receiver.ifPresent(receive -> receive.send(message, ALWAYS_SEND));
    }

    @Override
    public void close() throws InterruptedException, IOException {
        tickThread.interrupt();
        tickThread.join();

        sequencer.close();
        resetReceiver();
        output.ifPresent(MidiDevice::close);

        Optional<IOException> exception = Optional.empty();
        for (final Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (final IOException e) {
                if (exception.isPresent()) {
                    exception.get().addSuppressed(e);
                } else {
                    exception = Optional.of(e);
                }
            }
        }
        if (exception.isPresent()) {
            throw exception.get();
        }
    }

    private synchronized void resetReceiver() {
        try {
            for (int channel = 0; channel != 16; channel++) {
                // All notes off, reset all controllers, reset programs
                sendMessage(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 123, 0));
                sendMessage(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 121, 0));
                sendMessage(new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, 0, 0));
            }
        } catch (final InvalidMidiDataException e) {
            // Unreachable
            throw new IllegalStateException(e);
        }
    }
}
