package net.kreatious.pianoleopard.painter.layout;

import java.awt.Dimension;
import java.awt.Rectangle;

import net.kreatious.pianoleopard.midi.event.Event;
import net.kreatious.pianoleopard.midi.event.EventPair;

/**
 * Provides the strategy used to vertically layout notes on screen
 *
 * @author Jay-R Studer
 */
interface YStrategy {
    /**
     * Performs the vertical layout operation.
     * <p>
     * Implementors are to modify the y and height values of {@code rect}.
     *
     * @param currentTime
     *            the current song time in microseconds
     * @param event
     *            the event pair to layout
     * @param rect
     *            output parameter storing the resulting on screen location of
     *            the event.
     */
    void layout(long currentTime, EventPair<? extends Event> event, Rectangle rect);

    /**
     * Sets the component dimensions to layout within
     *
     * @param dimension
     *            the new dimensions of the component
     */
    void setComponentDimensions(Dimension dimension);

    /**
     * Calculates the highest visible time that can be laid out and still fit
     * within the current layout.
     *
     * @param currentTime
     *            the current song time in microseconds
     * @return the lowest visible time in microseconds that can be laid out
     */
    long getHighestVisibleTime(long currentTime);

    /**
     * Calculates the lowest visible time that can be laid out and still fit
     * within the current layout.
     *
     * @param currentTime
     *            the current song time in microseconds
     * @return the lowest visible time in microseconds that can be laid out
     */
    long getLowestVisibleTime(long currentTime);
}
