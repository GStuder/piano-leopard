package net.kreatious.pianoleopard.painter;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import net.kreatious.pianoleopard.midi.ParsedTrack;
import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.NoteEvent;
import net.kreatious.pianoleopard.midi.event.PedalEvent;
import net.kreatious.pianoleopard.painter.layout.DefaultEventLayout;
import net.kreatious.pianoleopard.painter.layout.EventLayout;
import net.kreatious.pianoleopard.painter.layout.PlayedEventLayout;

/**
 * Paints events into a graphics context using a predetermined layout strategy.
 *
 * @author Jay-R Studer
 */
class EventPainter {
    private final StyleStrategy<NoteEvent> noteStrategy;
    private final StyleStrategy<PedalEvent> pedalStrategy;
    private final EventLayout layout;

    private final Rectangle rect = new Rectangle();

    /**
     * Constructor declared private to prevent direct instantiation by
     * consumers.
     */
    private EventPainter(StyleStrategy<NoteEvent> noteStrategy, StyleStrategy<PedalEvent> pedalStrategy,
            EventLayout layout) {
        this.noteStrategy = noteStrategy;
        this.pedalStrategy = pedalStrategy;
        this.layout = layout;
    }

    /**
     * Retrieves the visible note pairs in a given track
     *
     * @param track
     *            the parsed track to retrieve visible note pairs from
     * @param currentTime
     *            the current song time in microseconds
     * @return an iterable read only view of visible note pairs contained in the
     *         specified track
     */
    Iterable<EventPair<NoteEvent>> getVisibleNotePairs(ParsedTrack track, long currentTime) {
        return track.getNotePairs(layout.getLowestVisibleTime(currentTime), layout.getHighestVisibleTime(currentTime));
    }

    /**
     * Retrieves the visible pedal pairs in a given track
     *
     * @param track
     *            the parsed track to retrieve visible pedal pairs from
     * @param currentTime
     *            the current song time in microseconds
     * @return an iterable read only view of visible pedal pairs contained in
     *         the specified track
     */
    Iterable<EventPair<PedalEvent>> getVisiblePedalPairs(ParsedTrack track, long currentTime) {
        return track.getPedalPairs(layout.getLowestVisibleTime(currentTime), layout.getHighestVisibleTime(currentTime));
    }

    /**
     * Constructs a new event painter for painting active events.
     *
     * @param dimension
     *            the initial component dimensions
     * @return a new {@link EventPainter}
     */
    static EventPainter createActiveEventPainter(Dimension dimension) {
        return new EventPainter(new ActiveNoteStyleStrategy(), new ActivePedalStyleStrategy(), new DefaultEventLayout(
                dimension));
    }

    /**
     * Constructs a new event painter for painting played events.
     *
     * @param dimension
     *            the initial component dimensions
     * @return a new {@link EventPainter}
     */
    static EventPainter createPlayedEventPainter(Dimension dimension) {
        return new EventPainter(new PlayedNoteStyleStrategy(), new PlayedPedalStyleStrategy(), new PlayedEventLayout(
                dimension));
    }

    /**
     * Paints the specified event into a graphics context
     *
     * @param currentTime
     *            the current song time in microseconds
     * @param graphics
     *            the graphics context to paint into
     * @param track
     *            the track to paint
     */
    void paint(long currentTime, Graphics2D graphics, ParsedTrack track) {
        final long low = layout.getLowestVisibleTime(currentTime);
        final long high = layout.getHighestVisibleTime(currentTime);

        for (final EventPair<PedalEvent> pedal : track.getPedalPairs(low, high)) {
            layout.layoutPedal(currentTime, pedal, rect);
            pedalStrategy.paint(graphics, pedal, rect);
        }

        for (final EventPair<NoteEvent> note : track.getNotePairs(low, high)) {
            layout.layoutNote(currentTime, note, rect);
            noteStrategy.paint(graphics, note, rect);
        }
    }

    /**
     * Resizes the layout to fit the specified component dimensions.
     *
     * @param dimension
     *            the new component layout dimensions
     */
    void setComponentDimensions(Dimension dimension) {
        layout.setComponentDimensions(dimension);
    }
}
