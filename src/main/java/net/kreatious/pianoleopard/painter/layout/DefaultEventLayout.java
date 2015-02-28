package net.kreatious.pianoleopard.painter.layout;

import java.awt.Dimension;
import java.awt.Rectangle;

import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.NoteEvent;
import net.kreatious.pianoleopard.midi.event.PedalEvent;

/**
 * Provides the default layout coordinates used for drawing events.
 *
 * @author Jay-R Studer
 */
public class DefaultEventLayout implements EventLayout {
    private final YStrategy yStrategy = new DownwardsYStrategy();
    private final XNoteStrategy xNoteStrategy = new DefaultXNoteStrategy();
    private final XPedalStrategy xPedalStrategy = new DefaultXPedalStrategy();

    /**
     * Constructs a new {@link DefaultEventLayout} with the specified initial
     * component dimensions.
     *
     * @param componentDimension
     *            the initial component layout dimensions
     */
    public DefaultEventLayout(Dimension componentDimension) {
        setComponentDimensions(componentDimension);
    }

    @Override
    public void layoutNote(long currentTime, EventPair<NoteEvent> event, Rectangle rect) {
        yStrategy.layout(currentTime, event, rect);
        xNoteStrategy.layout(event.getOn(), rect);
    }

    @Override
    public void layoutPedal(long currentTime, EventPair<PedalEvent> event, Rectangle rect) {
        yStrategy.layout(currentTime, event, rect);
        xPedalStrategy.layout(event.getOn(), rect);
    }

    @Override
    public void setComponentDimensions(Dimension dimension) {
        yStrategy.setComponentDimensions(dimension);
        xNoteStrategy.setComponentDimensions(dimension);
        xPedalStrategy.setComponentDimensions(dimension);
    }

    @Override
    public long getHighestVisibleTime(long currentTime) {
        return yStrategy.getHighestVisibleTime(currentTime);
    }

    @Override
    public long getLowestVisibleTime(long currentTime) {
        return yStrategy.getLowestVisibleTime(currentTime);
    }
}
