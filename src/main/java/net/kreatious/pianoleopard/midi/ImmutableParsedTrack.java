package net.kreatious.pianoleopard.midi;

import javax.sound.midi.Track;

import net.kreatious.pianoleopard.intervalset.IntervalSet;
import net.kreatious.pianoleopard.midi.event.Event;
import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.NoteEvent;
import net.kreatious.pianoleopard.midi.event.PedalEvent;
import net.kreatious.pianoleopard.midi.event.TempoCache;

/**
 * Represents a parsed MIDI track with efficient retrieval by time range.
 *
 * @author Jay-R Studer
 */
class ImmutableParsedTrack implements ParsedTrack {
    private final IntervalSet<Long, EventPair<NoteEvent>> notes;
    private final IntervalSet<Long, EventPair<PedalEvent>> pedals;

    /**
     * Constructs a new {@link ImmutableParsedTrack} by parsing the specified
     * {@link Track}.
     *
     * @param track
     *            the MIDI track to parse
     * @param cache
     *            the {@link TempoCache} to convert ticks into microseconds
     */
    ImmutableParsedTrack(Track track, TempoCache cache) {
        notes = new IntervalSet<>();
        pedals = new IntervalSet<>();
        new TrackVisitor() {
            @Override
            @SuppressWarnings("unchecked")
            protected void visitEventPair(EventPair<? extends Event> pair) {
                if (pair.getOff() instanceof NoteEvent) {
                    notes.put(pair.getOnTime(), pair.getOffTime(), (EventPair<NoteEvent>) pair);
                } else if (pair.getOff() instanceof PedalEvent) {
                    pedals.put(pair.getOnTime(), pair.getOffTime(), (EventPair<PedalEvent>) pair);
                }
            }
        }.accept(track, cache);
    }

    @Override
    public Iterable<EventPair<NoteEvent>> getNotePairs(long low, long high) {
        return notes.subSet(low, high);
    }

    @Override
    public Iterable<EventPair<PedalEvent>> getPedalPairs(long low, long high) {
        return pedals.subSet(low, high);
    }
}
