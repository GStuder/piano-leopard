package net.kreatious.pianoleopard.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.PedalEvent;

/**
 * Provides the drawing method for active pedal events
 *
 * @author Jay-R Studer
 */
class ActivePedalStyleStrategy implements StyleStrategy<PedalEvent> {
    private static final Stroke STROKE = new BasicStroke(2.0f);

    @Override
    public void paint(Graphics2D graphics, EventPair<PedalEvent> event, Rectangle rect) {
        graphics.setColor(transparent(getPedalColor(event)));
        graphics.setStroke(STROKE);
        graphics.fillRect(rect.x, rect.y, rect.width, rect.height);

        graphics.setColor(getPedalColor(event));
        graphics.drawRect(rect.x, rect.y, rect.width, rect.height);
    }

    private static Color getPedalColor(EventPair<PedalEvent> pedal) {
        switch (pedal.getOff().getPedal()) {
        case PORTAMENTO:
            return Color.BLUE;
        case SOFT:
            return Color.DARK_GRAY;
        case SOSTENUTO:
            return Color.RED;
        case SUSTAIN:
            return Color.GREEN;
        default:
            return Color.YELLOW;
        }
    }

    private static Color transparent(Color color) {
        return new Color(color.getRGB() & 0x30FFFFFF, true);
    }
}
