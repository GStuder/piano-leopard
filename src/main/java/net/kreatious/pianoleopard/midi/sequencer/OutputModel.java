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
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

import net.kreatious.pianoleopard.midi.ParsedSequence;

import com.google.common.annotations.VisibleForTesting;

/**
 * Model for the MIDI output sequencer, allows controllers to listen for events.
 *
 * @author Jay-R Studer
 */
public class OutputModel implements AutoCloseable {
    private final Sequencer sequencer;
    private ParsedSequence sequence;
    private Optional<MidiDevice> output = Optional.empty();
    private Optional<Receiver> receiver = Optional.empty();

    private final List<Consumer<? super ParsedSequence>> startListeners = new CopyOnWriteArrayList<>();
    private final List<LongConsumer> currentTimeListeners = new CopyOnWriteArrayList<>();

    private final Thread tickThread = new Thread() {
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
     *
     * @param output
     *            The initial output MIDI device to connect to
     * @param sequencerFactory
     *            A factory for producing the {@link Sequencer}, such as
     *            {@link SystemSequencerFactory}.
     * @throws MidiUnavailableException
     *             if the MIDI system is unavailable.
     */
    public OutputModel(MidiDevice output, SequencerFactory sequencerFactory) throws MidiUnavailableException {
        sequencer = sequencerFactory.getSequencer();
        setOutputDevice(output);
    }

    /**
     * Reconnects the sequencer to a different MIDI output device.
     *
     * @param output
     *            the new output MIDI device to reconnect to
     * @throws MidiUnavailableException
     *             if the MIDI system is unavailable.
     */
    public void setOutputDevice(MidiDevice output) throws MidiUnavailableException {
        sequencer.close();
        this.output.ifPresent(MidiDevice::close);
        this.output = Optional.of(output);

        output.open();
        receiver = Optional.of(output.getReceiver());
        sequencer.getTransmitter().setReceiver(receiver.get());
        sequencer.open();
    }

    /**
     * Starts playback of the currently loaded MIDI file.
     */
    public void start() {
        sequencer.stop();
        sequencer.setMicrosecondPosition(0);
        receiver.ifPresent(OutputModel::resetReceiver);
        startListeners.forEach(listener -> listener.accept(sequence));
        sequencer.start();
    }

    /**
     * Parses a MIDI file and prepares it for playback.
     *
     * @param midi
     *            the MIDI file to open
     * @throws IOException
     *             if an I/O error occurs
     */
    public void openMidiFile(File midi) throws IOException {
        try (InputStream in = new FileInputStream(midi)) {
            openMidiFile(in);
        }
    }

    @VisibleForTesting
    void openMidiFile(InputStream midiStream) throws IOException {
        try {
            if (tickThread.getState() == State.NEW) {
                tickThread.start();
            }

            sequence = ParsedSequence.parseByTracks(MidiSystem.getSequence(midiStream));
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
    public void sendMessage(MidiMessage message) {
        receiver.ifPresent(receive -> receive.send(message, -1));
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
                receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 123, 0), -1);
                receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 121, 0), -1);
                receiver.send(new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, 0, 0), -1);
            }
        } catch (final InvalidMidiDataException e) {
            // Unreachable
            e.printStackTrace();
        }
    }
}
