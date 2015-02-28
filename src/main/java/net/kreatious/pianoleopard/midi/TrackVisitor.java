package net.kreatious.pianoleopard.midi;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.sound.midi.Track;

import net.kreatious.pianoleopard.midi.event.Event;
import net.kreatious.pianoleopard.midi.event.EventFactory;
import net.kreatious.pianoleopard.midi.event.EventPair;

/**
 * Decomposes a {@link Track} into {@link EventPair}s
 *
 * @author Jay-R Studer
 */
abstract class TrackVisitor {
    private final Map<Object, Event> incompleteNotes = new HashMap<>();

    /**
     * Invokes this visitor on the specified track.
     *
     * @param track
     *            the {@link Track} to visit
     * @param cache
     *            the {@link TempoCache} to use for converting timestamps
     */
    public void accept(Track track, TempoCache cache) {
        for (int i = 0; i != track.size(); i++) {
            EventFactory.create(track.get(i), cache).ifPresent(
                    event -> {
                        if (event.isOn()) {
                            incompleteNotes.put(event.getSlot(), event);
                        } else {
                            Optional.ofNullable(incompleteNotes.remove(event.getSlot())).ifPresent(
                                    previousOnEvent -> visitEventPair(new EventPair<>(previousOnEvent, event)));
                        }
                    });
        }
    }

    /**
     * Called for each complete pair of events in the current track
     *
     * @param pair
     *            a completed event pair
     */
    protected abstract void visitEventPair(EventPair<? extends Event> pair);
}
