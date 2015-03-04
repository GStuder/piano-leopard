package net.kreatious.pianoleopard.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.PedalEvent;

/**
 * Provides the drawing method for active pedal events
 *
 * @author Jay-R Studer
 */
class InactivePedalStyleStrategy implements StyleStrategy<PedalEvent> {
    @Override
    public void paint(Graphics2D graphics, EventPair<PedalEvent> event, Rectangle rect) {
        graphics.setColor(transparent(ActivePedalStyleStrategy.getPedalColor(event)));
        graphics.fillRect(rect.x, rect.y, rect.width, rect.height);
    }

    private static Color transparent(Color color) {
        return new Color(color.getRGB() & 0x10FFFFFF, true);
    }
}
