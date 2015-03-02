package net.kreatious.pianoleopard.midi.event;

import java.util.Optional;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;

import net.kreatious.pianoleopard.midi.TempoCache;

/**
 * Creates {@link Event} subclasses from {@link MidiEvent}s
 *
 * @author Jay-R Studer
 */
public class EventFactory {
    /**
     * Private constructor to prevent instantiation by external consumers
     */
    private EventFactory() {
    }

    /**
     * Constructs a new immutable subclass of {@link Event} with the appropriate
     * information.
     *
     * @param event
     *            the {@link MidiEvent} to create an event for
     * @param cache
     *            the {@link TempoCache} to convert timestamps with
     * @return An optional containing a supported event type, otherwise an empty
     *         optional.
     */
    public static Optional<Event> create(MidiEvent event, TempoCache cache) {
        if (event.getMessage() instanceof ShortMessage == false) {
            return Optional.empty();
        }

        final ShortMessage message = (ShortMessage) event.getMessage();
        final long time = cache.ticksToMicroseconds(event.getTick()) + message.getData1() + (message.getChannel() << 8);
        if (NoteEvent.canCreate(message)) {
            return Optional.of(new NoteEvent(message, time));
        } else if (PedalEvent.canCreate(message)) {
            return Optional.of(new PedalEvent(message, time));
        } else {
            return Optional.empty();
        }
    }
}
