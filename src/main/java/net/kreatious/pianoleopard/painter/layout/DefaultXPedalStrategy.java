package net.kreatious.pianoleopard.painter.layout;

import java.awt.Dimension;
import java.awt.Rectangle;

import net.kreatious.pianoleopard.midi.event.PedalEvent;

/**
 * Provides the default layout strategy for pedal events. Lays out pedal events
 * such that they cover the entire width of the component.
 *
 * @author Jay-R Studer
 */
class DefaultXPedalStrategy implements XPedalStrategy {
    private int width;

    @Override
    public void layout(PedalEvent pedal, Rectangle rect) {
        rect.x = 0;
        rect.width = width;
    }

    @Override
    public void setComponentDimensions(Dimension dimension) {
        width = dimension.width;
    }
}
