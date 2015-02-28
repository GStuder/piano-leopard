package net.kreatious.pianoleopard.painter.layout;

import java.awt.Dimension;
import java.awt.Rectangle;

import net.kreatious.pianoleopard.midi.event.PedalEvent;

/**
 * Provides the strategy used to horizontally layout pedals on screen
 *
 * @author Jay-R Studer
 */
interface XPedalStrategy {

    /**
     * Performs the horizontal layout operation.
     * <p>
     * Implementors are to modify the x and width values of {@code rect}.
     *
     * @param pedal
     *            the pedal event to layout
     * @param rect
     *            output parameter storing the resulting on screen location of
     *            the event.
     */
    void layout(PedalEvent pedal, Rectangle rect);

    /**
     * Sets the component dimensions to layout within
     *
     * @param dimension
     *            the new dimensions of the component
     */
    void setComponentDimensions(Dimension dimension);

    /**
     * Decorates this strategy with the specified strategy.
     *
     * @param next
     *            the next strategy to apply after this strategy is called
     * @return a new decorated layout strategy
     */
    default XPedalStrategy thenApply(XPedalStrategy next) {
        return new XPedalStrategy() {
            private final XPedalStrategy xPedalStrategy = XPedalStrategy.this;

            @Override
            public void setComponentDimensions(Dimension dimension) {
                xPedalStrategy.setComponentDimensions(dimension);
                next.setComponentDimensions(dimension);
            }

            @Override
            public void layout(PedalEvent pedal, Rectangle rect) {
                xPedalStrategy.layout(pedal, rect);
                next.layout(pedal, rect);
            }
        };
    }
}
