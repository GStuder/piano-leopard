package net.kreatious.pianoleopard.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.NoteEvent;

/**
 * Provides the drawing method for notes being played by the user
 *
 * @author Jay-R Studer
 */
class PlayedNoteStyleStrategy implements StyleStrategy<NoteEvent> {
    private static final Color NATURAL_COLOR = new Color(0xFF8080);
    private static final Color SHARP_COLOR = new Color(0x800000);
    private static final Stroke STROKE = new BasicStroke(1.0f);

    @Override
    public void paint(Graphics2D graphics, EventPair<NoteEvent> event, Rectangle rect) {
        final boolean sharp = event.getOff().isSharp();
        graphics.setColor(sharp ? SHARP_COLOR : NATURAL_COLOR);
        graphics.setStroke(STROKE);
        graphics.fillRect(rect.x, rect.y, rect.width, rect.height);

        graphics.setColor(sharp ? NATURAL_COLOR : SHARP_COLOR);
        graphics.drawRect(rect.x, rect.y, rect.width, rect.height);
    }
}
