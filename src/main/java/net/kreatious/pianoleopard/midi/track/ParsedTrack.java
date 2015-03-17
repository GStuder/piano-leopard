package net.kreatious.pianoleopard.midi.track;

import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.NoteEvent;
import net.kreatious.pianoleopard.midi.event.PedalEvent;


/**
 * Provides an immutable view of a MIDI track with efficient retrieval by time
 * range.
 *
 * @author Jay-R Studer
 */
public interface ParsedTrack {
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
    Iterable<EventPair<NoteEvent>> getNotePairs(long low, long high);

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
    Iterable<EventPair<PedalEvent>> getPedalPairs(long low, long high);
}
