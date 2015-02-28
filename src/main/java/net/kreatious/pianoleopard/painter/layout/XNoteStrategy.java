package net.kreatious.pianoleopard.painter.layout;

import java.awt.Dimension;
import java.awt.Rectangle;

import net.kreatious.pianoleopard.midi.event.NoteEvent;

/**
 * Provides the strategy used to horizontally layout notes on screen
 *
 * @author Jay-R Studer
 */
interface XNoteStrategy {

    /**
     * Performs the horizontal layout operation.
     * <p>
     * Implementors are to modify the x and width values of {@code rect}.
     *
     * @param note
     *            the note event to layout
     * @param rect
     *            output parameter storing the resulting on screen location of
     *            the event.
     */
    void layout(NoteEvent note, Rectangle rect);

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
    default XNoteStrategy thenApply(XNoteStrategy next) {
        return new XNoteStrategy() {
            private final XNoteStrategy xNoteStrategy = XNoteStrategy.this;

            @Override
            public void setComponentDimensions(Dimension dimension) {
                xNoteStrategy.setComponentDimensions(dimension);
                next.setComponentDimensions(dimension);
            }

            @Override
            public void layout(NoteEvent note, Rectangle rect) {
                xNoteStrategy.layout(note, rect);
                next.layout(note, rect);
            }
        };
    }
}
