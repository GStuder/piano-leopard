package net.kreatious.pianoleopard.painter;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import net.kreatious.pianoleopard.midi.event.Event;
import net.kreatious.pianoleopard.midi.event.EventPair;

/**
 * Draws an event to the screen
 *
 * @param <T>
 *            the type of event handled by this strategy
 * @author Jay-R Studer
 */
interface StyleStrategy<T extends Event> {
    /**
     * Paints into the specified graphics context the specified event in the
     * appropriate color.
     *
     * @param graphics
     *            the graphics class to set color and stroke information on
     * @param event
     *            the current event being rendered
     * @param rect
     *            the location to draw the event at
     */
    void paint(Graphics2D graphics, EventPair<T> event, Rectangle rect);
}
