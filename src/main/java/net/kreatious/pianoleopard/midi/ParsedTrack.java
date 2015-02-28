package net.kreatious.pianoleopard.midi;

import javax.sound.midi.Track;

import net.kreatious.pianoleopard.intervalset.IntervalSet;
import net.kreatious.pianoleopard.midi.event.Event;
import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.NoteEvent;
import net.kreatious.pianoleopard.midi.event.PedalEvent;

/**
 * Represents a parsed MIDI track with efficient retrieval by time range.
 *
 * @author Jay-R Studer
 */
public class ParsedTrack {
    private final IntervalSet<Long, EventPair<NoteEvent>> notes = new IntervalSet<>();
    private final IntervalSet<Long, EventPair<PedalEvent>> pedals = new IntervalSet<>();

    /**
     * Constructs a new {@link ParsedTrack} by parsing the specified
     * {@link Track}.
     *
     * @param track
     *            the MIDI track to parse
     * @param cache
     *            the {@link TempoCache} to convert ticks into microseconds
     */
    ParsedTrack(Track track, TempoCache cache) {
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

    /**
     * Gets the note event pairs overlapping with the specified interval
     *
     * @param low
     *            the lower inclusive bound to return events for in microseconds
     * @param high
     *            the upper inclusive bound to return events for in microseconds
     * @throws IllegalArgumentException
     *             if {@code low} is greater than {@code high}
     * @return a read only view of the portion of events overlapping the
     *         specified interval.
     */
    public Iterable<EventPair<NoteEvent>> getNotePairs(long low, long high) {
        return notes.subSet(low, high);
    }

    /**
     * Gets the pedal event pairs overlapping with the specified interval
     *
     * @param low
     *            the lower inclusive bound to return events for in microseconds
     * @param high
     *            the upper inclusive bound to return events for in microseconds
     * @throws IllegalArgumentException
     *             if {@code low} is greater than {@code high}
     * @return a read only view of the portion of events overlapping the
     *         specified interval.
     */
    public Iterable<EventPair<PedalEvent>> getPedalPairs(long low, long high) {
        return pedals.subSet(low, high);
    }
}
