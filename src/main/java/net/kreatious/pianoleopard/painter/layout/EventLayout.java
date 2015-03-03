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
}
