package net.kreatious.pianoleopard.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.PedalEvent;

/**
 * Provides the drawing method for pedal events being played by the user
 *
 * @author Jay-R Studer
 */
class PlayedPedalStyleStrategy implements StyleStrategy<PedalEvent> {
    private static final Stroke STROKE = new BasicStroke(2.0f);
    private int smallestSeenChannel = 16;

    @Override
    public void paint(Graphics2D graphics, EventPair<PedalEvent> event, Rectangle rect) {
        // Certain keyboards send pedal events to multiple channels
        if (event.getChannel() < smallestSeenChannel) {
            smallestSeenChannel = event.getChannel();
            return;
        } else if (event.getChannel() != smallestSeenChannel) {
            return;
        }

        graphics.setColor(transparent(ActivePedalStyleStrategy.getPedalColor(event)));
        graphics.setStroke(STROKE);
        graphics.fillRect(rect.x, rect.y, rect.width, rect.height);

        graphics.setColor(ActivePedalStyleStrategy.getPedalColor(event));
        graphics.drawRect(rect.x, rect.y, rect.width, rect.height);
    }

    private static Color transparent(Color color) {
        return new Color(color.getRGB() & 0x30FFFFFF, true);
    }
}
