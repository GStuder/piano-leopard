package net.kreatious.pianoleopard.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Paints the foreground into a graphics context
 *
 * @author Jay-R Studer
 */
class ForegroundPainter {
    private static final Stroke STROKE = new BasicStroke(2);
    private int playBarY;
    private int width;

    /**
     * Constructor declared private to prevent direct instantiation by
     * consumers.
     */
    private ForegroundPainter(Dimension dimension) {
        setComponentDimensions(dimension);
    }

    /**
     * Constructs a new foreground painter.
     *
     * @param dimension
     *            the initial component dimensions
     * @return a new {@link ForegroundPainter}
     */
    static ForegroundPainter create(Dimension dimension) {
        return new ForegroundPainter(dimension);
    }

    /**
     * Paints the foreground into a graphics context
     *
     * @param graphics
     *            the graphics context to paint into
     */
    void paint(Graphics2D graphics) {
        graphics.setColor(Color.RED);
        graphics.setStroke(STROKE);
        graphics.drawLine(0, playBarY, width, playBarY);
    }

    /**
     * Sets the component dimensions to layout within
     *
     * @param dimension
     *            the new dimensions of the component
     */
    void setComponentDimensions(Dimension dimension) {
        width = dimension.width;
        playBarY = dimension.height / 2;
    }
}
