package net.kreatious.pianoleopard.painter.layout;

import java.awt.Dimension;
import java.awt.Rectangle;

import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.NoteEvent;
import net.kreatious.pianoleopard.midi.event.PedalEvent;

/**
 * Provides the layout coordinates used for drawing events.
 *
 * @author Jay-R Studer
 */
public interface EventLayout {
    /**
     * Lays out the coordinates for drawing the specified note event.
     *
     * @param currentTime
     *            the current song time in microseconds
     * @param event
     *            the note event pair to layout
     * @param rect
     *            output parameter storing the resulting on screen location of
     *            the event.
     */
    void layoutNote(long currentTime, EventPair<NoteEvent> event, Rectangle rect);

    /**
     * Lays out the coordinates for drawing the specified pedal event.
     *
     * @param currentTime
     *            the current song time in microseconds
     * @param event
     *            the pedal event pair to layout
     * @param rect
     *            output parameter storing the resulting on screen location of
     *            the event.
     */
    void layoutPedal(long currentTime, EventPair<PedalEvent> event, Rectangle rect);

    /**
     * Resizes the layout to fit the specified component dimensions.
     *
     * @param dimension
     *            the new component layout dimensions
     */
    void setComponentDimensions(Dimension dimension);

    /**
     * Calculates the lowest visible time that can be laid out and still fit
     * within the current layout.
     *
     * @param currentTime
     *            the current song time in microseconds
     * @return the lowest visible time in microseconds that can be laid out
     */
    long getLowestVisibleTime(long currentTime);

    /**
     * Calculates the highest visible time that can be laid out and still fit
     * within the current layout.
     *
     * @param currentTime
     *            the current song time in microseconds
     * @return the highest visible time in microseconds that can be laid out
     */
    long getHighestVisibleTime(long currentTime);

    /**
     * Decorates this strategy with the specified strategy.
     *
     * @param next
     *            the next strategy to apply after this strategy is called
     * @return a new decorated layout strategy
     */
    default EventLayout thenApply(EventLayout next) {
        return new EventLayout() {
            private final EventLayout eventLayout = EventLayout.this;

            @Override
            public void layoutNote(long currentTime, EventPair<NoteEvent> event, Rectangle rect) {
                eventLayout.layoutNote(currentTime, event, rect);
                next.layoutNote(currentTime, event, rect);
            }

            @Override
            public void layoutPedal(long currentTime, EventPair<PedalEvent> event, Rectangle rect) {
                eventLayout.layoutPedal(currentTime, event, rect);
                next.layoutPedal(currentTime, event, rect);
            }

            @Override
            public void setComponentDimensions(Dimension dimension) {
                eventLayout.setComponentDimensions(dimension);
                next.setComponentDimensions(dimension);
            }

            @Override
            public long getLowestVisibleTime(long currentTime) {
                return eventLayout.getLowestVisibleTime(currentTime);
            }

            @Override
            public long getHighestVisibleTime(long currentTime) {
                return eventLayout.getHighestVisibleTime(currentTime);
            }
        };
    }
}
