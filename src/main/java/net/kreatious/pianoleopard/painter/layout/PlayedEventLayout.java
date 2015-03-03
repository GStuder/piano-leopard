package net.kreatious.pianoleopard.painter.layout;

import java.awt.Dimension;
import java.awt.Rectangle;

import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.NoteEvent;

/**
 * Provides the coordinates for drawing events played by the user.
 *
 * @author Jay-R Studer
 */
public class PlayedEventLayout extends DefaultEventLayout {
    /**
     * Constructs a new {@link PlayedEventLayout} with the initial specified
     * component layout dimensions
     *
     * @param componentDimension
     *            the inital component layout dimensions
     */
    public PlayedEventLayout(Dimension componentDimension) {
        super(componentDimension);
    }

    @Override
    public void layoutNote(long currentTime, EventPair<NoteEvent> event, Rectangle rect) {
        super.layoutNote(currentTime, event, rect);
        rect.x += rect.width / 4;
        rect.width -= rect.width / 2;
    }
}
