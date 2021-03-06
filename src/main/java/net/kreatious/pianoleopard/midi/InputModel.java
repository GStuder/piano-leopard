package net.kreatious.pianoleopard.midi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

import net.kreatious.pianoleopard.intervalset.IntervalSet;
import net.kreatious.pianoleopard.midi.event.Event;
import net.kreatious.pianoleopard.midi.event.EventFactory;
import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.NoteEvent;
import net.kreatious.pianoleopard.midi.event.PedalEvent;
import net.kreatious.pianoleopard.midi.track.ParsedSequence;
import net.kreatious.pianoleopard.midi.track.ParsedTrack;

/**
 * Model for the MIDI input keyboard, allows controllers to listen for events.
 *
 * @author Jay-R Studer
 */
public class InputModel implements AutoCloseable, ParsedTrack {
    private Optional<MidiDevice> input = Optional.empty();
    private final UserNoteRecorder userRecorder = new UserNoteRecorder();

    private final IntervalSet<EventPair<NoteEvent>> notes = new IntervalSet<>();
    private final IntervalSet<EventPair<PedalEvent>> pedals = new IntervalSet<>();

    private final List<Consumer<? super Info>> inputDeviceListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<? super Event>> inputListeners = new CopyOnWriteArrayList<>();

    private InputModel(MidiDevice input) throws MidiUnavailableException {
        setInputDevice(input);
    }

    /**
     * Constructs a new {@link InputModel} with the specified initial state.
     * <p>
     * After construction, the input model is not connected to any actual
     * devices. It is expected that the consumer will change the input device.
     *
     * @param outputModel
     *            the output model to coordinate with
     * @return a new instance of {@link InputModel}. The caller is responsible
     *         for releasing the resource.
     * @throws MidiUnavailableException
     *             if the MIDI system is unavailable.
     */
    public static InputModel create(OutputModel outputModel) throws MidiUnavailableException {
        final InputModel input = new InputModel(new InitialMidiDevice());
        outputModel.addOpenListener(input::setCurrentSequence);
        outputModel.addPlayListener(input.userRecorder::clear);
        outputModel.addCurrentTimeListener(input.userRecorder::setCurrentTime);
        return input;
    }

    private void setCurrentSequence(@SuppressWarnings("unused") ParsedSequence sequence) {
        userRecorder.clear();
    }

    private final class UserNoteRecorder implements Receiver, ParsedTrack {
        private final Map<Object, NoteEvent> onNotes = new HashMap<>();
        private final Map<Object, PedalEvent> onPedals = new HashMap<>();

        private long currentTime;

        private synchronized void setCurrentTime(long time) {
            currentTime = time;
        }

        @Override
        public synchronized void send(MidiMessage message, long timeStamp) {
            EventFactory.create(message, currentTime).ifPresent(this::userPressedEvent);
        }

        private void userPressedEvent(Event event) {
            if (event instanceof NoteEvent) {
                userPressedEvent((NoteEvent) event, onNotes, notes);
            } else if (event instanceof PedalEvent) {
                userPressedEvent((PedalEvent) event, onPedals, pedals);
            }
            inputListeners.forEach(listener -> listener.accept(event));
        }

        private <K extends Event> void userPressedEvent(K event, Map<Object, K> onEvents,
                IntervalSet<EventPair<K>> fullEvents) {
            synchronized (fullEvents) {
                final long eventTime = event.getTime();
                if (event.isOn()) {
                    onEvents.put(event.getSlot(), event);
                } else {
                    Optional.ofNullable(onEvents.remove(event.getSlot())).ifPresent(onEvent -> {
                        fullEvents.put(onEvent.getTime(), eventTime, new EventPair<>(onEvent, event));
                    });
                }
            }
        }

        @Override
        public Iterable<EventPair<NoteEvent>> getNotePairs(long low, long high) {
            return getPairs(low, high, onNotes, notes);
        }

        @Override
        public Iterable<EventPair<PedalEvent>> getPedalPairs(long low, long high) {
            return getPairs(low, high, onPedals, pedals);
        }

        private synchronized <K extends Event> Iterable<EventPair<K>> getPairs(long low, long high,
                Map<Object, K> onEvents, IntervalSet<EventPair<K>> fullEvents) {
            synchronized (fullEvents) {
                final List<EventPair<K>> result = new ArrayList<>();
                fullEvents.subSet(low, high).forEach(result::add);
                onEvents.values().forEach(event -> result.add(new EventPair<>(event, event.createOff(currentTime))));
                return result;
            }
        }

        void clear() {
            synchronized (notes) {
                notes.clear();
                onNotes.clear();
            }
            synchronized (pedals) {
                pedals.clear();
                onPedals.clear();
            }
        }

        @Override
        public void close() {
            // Intentionally empty; this receiver holds no system resources
        }
    }

    /**
     * Reconnects the input to a different MIDI input device.
     *
     * @param input
     *            the new input MIDI device to reconnect to
     * @throws MidiUnavailableException
     *             if the MIDI system is unavailable.
     */
    public void setInputDevice(MidiDevice input) throws MidiUnavailableException {
        this.input.ifPresent(MidiDevice::close);
        this.input = Optional.of(input);

        input.open();
        input.getTransmitter().setReceiver(userRecorder);
        userRecorder.clear();
        inputDeviceListeners.forEach(listener -> listener.accept(input.getDeviceInfo()));
    }

    /**
     * Adds a listener to notify when the input device has changed.
     *
     * @param listener
     *            the listener to add
     */
    public void addInputDeviceListener(Consumer<? super Info> listener) {
        inputDeviceListeners.add(listener);
    }

    /**
     * Adds a listener to notify when the user has pressed a key
     *
     * @param listener
     *            the listener to add
     */
    public void addInputListener(Consumer<? super Event> listener) {
        inputListeners.add(listener);
    }

    @Override
    public void close() {
        input.ifPresent(MidiDevice::close);
    }

    @Override
    public Iterable<EventPair<NoteEvent>> getNotePairs(long low, long high) {
        return userRecorder.getNotePairs(low, high);
    }

    @Override
    public Iterable<EventPair<PedalEvent>> getPedalPairs(long low, long high) {
        return userRecorder.getPedalPairs(low, high);
    }
}
