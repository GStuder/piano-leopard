package net.kreatious.pianoleopard.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.NoteEvent;

/**
 * Provides the drawing method for active notes
 *
 * @author Jay-R Studer
 */
class ActiveNoteStyleStrategy implements StyleStrategy<NoteEvent> {
    private static final Stroke STROKE = new BasicStroke(1.0f);

    @Override
    public void paint(Graphics2D graphics, EventPair<NoteEvent> event, Rectangle rect) {
        final boolean sharp = event.getOff().isSharp();
        graphics.setColor(sharp ? Color.BLACK : Color.WHITE);
        graphics.setStroke(STROKE);
        graphics.fillRect(rect.x, rect.y, rect.width, rect.height);

        graphics.setColor(sharp ? Color.WHITE : Color.BLACK);
        graphics.drawRect(rect.x, rect.y, rect.width, rect.height);
    }
}
