package net.kreatious.pianoleopard.midi.sequencer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.State;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
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
import net.kreatious.pianoleopard.midi.ParsedTrack;
import net.kreatious.pianoleopard.midi.event.Event;
import net.kreatious.pianoleopard.midi.event.EventFactory;
import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.NoteEvent;
import net.kreatious.pianoleopard.midi.event.PedalEvent;
import net.kreatious.pianoleopard.midi.event.Slot;

import com.google.common.annotations.VisibleForTesting;

/**
 * Model for the MIDI output sequencer, allows controllers to listen for events.
 *
 * @author Jay-R Studer
 */
public class OutputModel implements AutoCloseable {
    private static final long ALWAYS_SEND = -10;
    private final Sequencer sequencer;
    private ParsedSequence sequence = ParsedSequence.createEmpty();
    private Optional<MidiDevice> output = Optional.empty();
    private Optional<Receiver> receiver = Optional.empty();
    private boolean playAlong;

    private final List<Consumer<? super Info>> outputDeviceListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<? super ParsedSequence>> startListeners = new CopyOnWriteArrayList<>();
    private final List<Runnable> playListeners = new CopyOnWriteArrayList<>();
    private final List<LongConsumer> currentTimeListeners = new CopyOnWriteArrayList<>();

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
        receiver.ifPresent(OutputModel::resetReceiver);
        startListeners.forEach(listener -> listener.accept(sequence));
        playListeners.forEach(Runnable::run);
        sequencer.start();
    }

    private class MutingReceiverProxy implements Receiver {
        private final long tolerance = TimeUnit.SECONDS.toMicros(2);
        private final Receiver wrapped;

        private MutingReceiverProxy(Receiver wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void send(MidiMessage message, long timeStamp) {
            if (timeStamp == ALWAYS_SEND) {
                wrapped.send(message, -1);
            } else if (message instanceof ShortMessage == false) {
                wrapped.send(message, timeStamp);
            } else if (EventFactory.create(message, sequencer.getMicrosecondPosition()).filter(this::isMuted)
                    .isPresent() == false) {
                wrapped.send(message, timeStamp);
            }
        }

        /**
         * An event is considered muted if its {@link Slot} is not found in any
         * inactive tracks at the current time.
         */
        private boolean isMuted(Event event) {
            // Never mute a note off event
            if (!event.isOn()) {
                return false;
            } else if (playAlong) {
                return false;
            }

            for (final ParsedTrack track : sequence.getInactiveTracks()) {
                final Iterable<? extends EventPair<? extends Event>> pairs;
                if (event instanceof NoteEvent) {
                    pairs = track.getNotePairs(event.getTime(), event.getTime() + tolerance);
                } else if (event instanceof PedalEvent) {
                    pairs = track.getPedalPairs(event.getTime(), event.getTime() + tolerance);
                } else {
                    return false;
                }

                for (final EventPair<? extends Event> pair : pairs) {
                    if (pair.getSlot().equals(event.getSlot())) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public void close() {
            wrapped.close();
        }
    }

    /**
     * Sets if active tracks are not muted.
     *
     * @param playAlong
     *            true if tracks are not to be muted, otherwise false.
     */
    public void setPlayAlong(boolean playAlong) {
        this.playAlong = playAlong;
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
            receiver.ifPresent(OutputModel::resetReceiver);
            startListeners.forEach(listener -> listener.accept(sequence));
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
     * Adds a listener to notify when a parsed MIDI file is started from the
     * beginning.
     *
     * @param listener
     *            the listener to add
     */
    public void addStartListener(Consumer<? super ParsedSequence> listener) {
        startListeners.add(listener);
    }

    /**
     * Adds a listener to notify when a parsed MIDI file is started from the
     * beginning.
     *
     * @param listener
     *            the listener to add
     */
    public void addPlayListener(Runnable listener) {
        playListeners.add(listener);
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
     * Sends a MIDI message to the output.
     *
     * @param message
     *            the MIDI message to send to the connected output device
     */
    public synchronized void sendMessage(MidiMessage message) {
        receiver.ifPresent(receive -> receive.send(message, ALWAYS_SEND));
    }

    @Override
    public void close() throws InterruptedException {
        tickThread.interrupt();
        tickThread.join();

        sequencer.close();
        receiver.ifPresent(OutputModel::resetReceiver);
        output.ifPresent(MidiDevice::close);
    }

    private static void resetReceiver(Receiver receiver) {
        try {
            for (int channel = 0; channel != 16; channel++) {
                // All notes off, reset all controllers, reset programs
                receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 123, 0), ALWAYS_SEND);
                receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 121, 0), ALWAYS_SEND);
                receiver.send(new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, 0, 0), ALWAYS_SEND);
            }
        } catch (final InvalidMidiDataException e) {
            // Unreachable
            throw new IllegalStateException(e);
        }
    }
}
