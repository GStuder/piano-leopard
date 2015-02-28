package net.kreatious.pianoleopard.painter;

import java.awt.Dimension;
import java.awt.Graphics2D;

import net.kreatious.pianoleopard.midi.ParsedSequence;
import net.kreatious.pianoleopard.midi.ParsedTrack;

/**
 * Paints a sequence of notes into a graphics context
 *
 * @author Jay-R Studer
 */
class Painter {
    private final BackgroundPainter backgroundPainter;
    private final EventPainter activeEventPainter;
    private final ForegroundPainter foregroundPainter;

    /**
     * Constructs a new {@link Painter} with the specified initial component
     * dimensions
     *
     * @param dimension
     *            the inital component dimensions to paint within
     */
    Painter(Dimension dimension) {
        backgroundPainter = BackgroundPainter.create(dimension);
        activeEventPainter = EventPainter.createActiveEventPainter(dimension);
        foregroundPainter = ForegroundPainter.create(dimension);
    }

    /**
     * Paints the specified MIDI sequence into the graphics context.
     *
     * @param graphics
     *            the graphics context to paint into
     * @param currentTime
     *            the current song time in microseconds
     * @param sequence
     *            the MIDI sequence to render
     */
    void paint(Graphics2D graphics, long currentTime, ParsedSequence sequence) {
        backgroundPainter.paint(graphics);

        for (final ParsedTrack track : sequence.getTracks()) {
            activeEventPainter.paint(currentTime, graphics, track);
        }

        foregroundPainter.paint(graphics);
    }

    /**
     * Sets the component dimensions to paint within
     *
     * @param dimension
     *            the new dimensions of the component
     */
    void setComponentDimensions(Dimension dimension) {
        backgroundPainter.setComponentDimensions(dimension);
        activeEventPainter.setComponentDimensions(dimension);
        foregroundPainter.setComponentDimensions(dimension);
    }
}
