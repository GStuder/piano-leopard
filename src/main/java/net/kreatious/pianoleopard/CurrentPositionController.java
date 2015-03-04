package net.kreatious.pianoleopard;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.concurrent.TimeUnit;

import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

import net.kreatious.pianoleopard.midi.sequencer.OutputModel;
import net.kreatious.pianoleopard.painter.layout.DefaultEventLayout;
import net.kreatious.pianoleopard.painter.layout.EventLayout;

/**
 * Provides control over the current song position
 *
 * @author Jay-R Studer
 */
class CurrentPositionController {
    private CurrentPositionController() {
    }

    /**
     * Constructs a view and associates it with its controller
     */
    static Component create(OutputModel outputModel) {
        final JScrollBar scrollBar = new JScrollBar();
        scrollBar.setBlockIncrement(convertTime(TimeUnit.SECONDS.toMicros(30)));
        scrollBar.setUnitIncrement(convertTime(TimeUnit.SECONDS.toMicros(1)));

        // Update track sizes
        outputModel.addStartListener(s -> scrollBar.setMaximum(convertTime(s.getSequence().getMicrosecondLength())));
        scrollBar.addComponentListener(new ComponentAdapter() {
            private final EventLayout layout = new DefaultEventLayout(scrollBar.getSize());

            @Override
            public void componentResized(ComponentEvent e) {
                layout.setComponentDimensions(scrollBar.getSize());
                final long visibleTime = layout.getHighestVisibleTime(0) - layout.getLowestVisibleTime(0);
                scrollBar.setVisibleAmount(convertTime(visibleTime));
            }
        });

        // Update thumb positions
        scrollBar.addAdjustmentListener(l -> {
            if (l.getValueIsAdjusting()) {
                outputModel.setCurrentTime(convertValue(l.getValue()));
            }
        });
        outputModel.addCurrentTimeListener(time -> SwingUtilities.invokeLater(() -> {
            if (!scrollBar.getValueIsAdjusting()) {
                scrollBar.setValue(convertTime(time));
            }
        }));
        return scrollBar;
    }

    private static long convertValue(int time) {
        return (long) time << 10;
    }

    private static int convertTime(long time) {
        // Rolls over after 50 days, provides millisecond accuracy
        return (int) (time >> 10);
    }
}
