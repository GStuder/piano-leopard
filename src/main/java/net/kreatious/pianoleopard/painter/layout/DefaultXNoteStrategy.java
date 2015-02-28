package net.kreatious.pianoleopard.painter.layout;

import java.awt.Dimension;
import java.awt.Rectangle;

import net.kreatious.pianoleopard.midi.event.NoteEvent;

/**
 * Provides the default horizontal layout strategy, laying out low notes on the
 * left and high notes on the right.
 *
 * @author Jay-R Studer
 */
class DefaultXNoteStrategy implements XNoteStrategy {
    private static final int LOWEST_PLAYABLE_KEY = 36;
    private static final int HIGHEST_PLAYABLE_KEY = 96;

    private float minXFactor;
    private float normalWidth;

    @Override
    public void layout(NoteEvent note, Rectangle rect) {
        final NoteSize size = NoteSize.forPitch(note.getKey());
        rect.x = (int) (normalWidth * (size.getXOffsetFactor() - minXFactor));
        rect.width = (int) (normalWidth * size.getWidthFactor());
    }

    @Override
    public void setComponentDimensions(Dimension dimension) {
        minXFactor = NoteSize.forPitch(LOWEST_PLAYABLE_KEY).getXOffsetFactor();

        final NoteSize highestSize = NoteSize.forPitch(HIGHEST_PLAYABLE_KEY);
        normalWidth = dimension.width / (highestSize.getXOffsetFactor() - minXFactor + highestSize.getWidthFactor());
    }
}
