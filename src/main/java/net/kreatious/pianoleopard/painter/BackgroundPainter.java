package net.kreatious.pianoleopard.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.stream.IntStream;

import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.NoteEvent;
import net.kreatious.pianoleopard.painter.layout.DefaultEventLayout;
import net.kreatious.pianoleopard.painter.layout.EventLayout;

/**
 * Paints the background into a graphics context
 *
 * @author Jay-R Studer
 */
class BackgroundPainter {
    private static final EventPair<NoteEvent>[] SHARP_NOTES = IntStream.range(0, 128)
            .mapToObj(key -> new EventPair<>(new NoteEvent(key, true, 0), new NoteEvent(key, false, 0)))
            .filter(pair -> pair.getOff().isSharp()).toArray(EventPair[]::new);

    private static final EventPair<NoteEvent>[] NATURAL_NOTES = IntStream.range(0, 128)
            .mapToObj(key -> new EventPair<>(new NoteEvent(key, true, 0), new NoteEvent(key, false, 0)))
            .filter(pair -> pair.getOff().isSharp() == false).toArray(EventPair[]::new);

    private static final Stroke STROKE = new BasicStroke();

    private final EventLayout layout;
    private final Rectangle rect = new Rectangle();

    private int width;
    private int height;

    /**
     * Constructor declared private to prevent direct instantiation by
     * consumers.
     */
    private BackgroundPainter(EventLayout layout, Dimension dimension) {
        this.layout = layout;
        width = dimension.width;
        height = dimension.height;
    }

    /**
     * Constructs a new background painter.
     *
     * @param dimension
     *            the initial component dimensions
     * @return a new {@link BackgroundPainter}
     */
    static BackgroundPainter create(Dimension dimension) {
        return new BackgroundPainter(new DefaultEventLayout(dimension), dimension);
    }

    /**
     * Paints the background into a graphics context
     *
     * @param graphics
     *            the graphics context to paint into
     */
    void paint(Graphics2D graphics) {
        paintNaturalNoteBackgrounds(graphics);
        paintLinesBetweenNaturalNotes(graphics);
        paintSharpNoteBackgrounds(graphics);
    }

    private void paintNaturalNoteBackgrounds(Graphics2D graphics) {
        graphics.setColor(new Color(160, 160, 160));
        graphics.fillRect(0, 0, width, height);
    }

    private void paintLinesBetweenNaturalNotes(Graphics2D graphics) {
        graphics.setColor(Color.BLACK);
        graphics.setStroke(STROKE);
        for (final EventPair<NoteEvent> sharpNote : NATURAL_NOTES) {
            layout.layoutNote(0, sharpNote, rect);
            graphics.drawLine(rect.x, 0, rect.x, height);
        }
    }

    private void paintSharpNoteBackgrounds(Graphics2D graphics) {
        graphics.setColor(new Color(96, 96, 96));
        for (final EventPair<NoteEvent> sharpNote : SHARP_NOTES) {
            layout.layoutNote(0, sharpNote, rect);
            graphics.fillRect(rect.x, 0, rect.width, height);
        }
    }

    /**
     * Resizes the layout to fit the specified component dimensions.
     *
     * @param dimension
     *            the new component layout dimensions
     */
    void setComponentDimensions(Dimension dimension) {
        layout.setComponentDimensions(dimension);
        width = dimension.width;
        height = dimension.height;
    }
}
