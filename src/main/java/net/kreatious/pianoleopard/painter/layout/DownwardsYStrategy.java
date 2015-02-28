package net.kreatious.pianoleopard.painter.layout;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.concurrent.TimeUnit;

import net.kreatious.pianoleopard.midi.event.Event;
import net.kreatious.pianoleopard.midi.event.EventPair;

/**
 * Lays out notes such that they move from the top of the screen downwards.
 *
 * @author Jay-R Studer
 */
class DownwardsYStrategy implements YStrategy {
    private static final int MICROS_PER_PIXEL = (int) TimeUnit.MILLISECONDS.toMicros(5);
    private int playBarY;

    @Override
    public void layout(long currentTime, EventPair<? extends Event> event, Rectangle rect) {
        rect.y = (int) ((event.getOnTime() - currentTime) / MICROS_PER_PIXEL);
        rect.height = (int) (event.getDuration() / MICROS_PER_PIXEL);
        rect.y = playBarY - rect.y - rect.height;
    }

    @Override
    public void setComponentDimensions(Dimension dimension) {
        playBarY = dimension.height / 2;
    }

    @Override
    public long getHighestVisibleTime(long currentTime) {
        return currentTime + playBarY * MICROS_PER_PIXEL;
    }

    @Override
    public long getLowestVisibleTime(long currentTime) {
        return currentTime - playBarY * MICROS_PER_PIXEL;
    }
}
