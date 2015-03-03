package net.kreatious.pianoleopard.painter;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

import net.kreatious.pianoleopard.midi.ParsedSequence;
import net.kreatious.pianoleopard.midi.ParsedTrack;

/**
 * Renders the currently playing sequence into a panel using double buffering.
 *
 * @author Jay-R Studer
 */
public class PainterPanel {
    private final class PainterPanelImpl extends JPanel {
        private static final long serialVersionUID = 3647951920113354307L;
        final Painter painter = new Painter(new Dimension());

        PainterPanelImpl() {
            setPreferredSize(new Dimension(800, 400));
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    painter.setComponentDimensions(getSize());
                }
            });
        }

        @Override
        public void paint(Graphics g) {
            painter.paint((Graphics2D) g, currentTime, sequence, playedTrack);
        }
    }

    private final JPanel panel = new PainterPanelImpl();
    private final ParsedTrack playedTrack;

    private volatile long currentTime;
    private volatile ParsedSequence sequence = ParsedSequence.createEmpty();

    /**
     * Constructs a new {@link PainterPanel} connected to the specified track
     * containing events played by the user
     *
     * @param playedTrack
     *            the track of events played by the user
     */
    public PainterPanel(ParsedTrack playedTrack) {
        this.playedTrack = playedTrack;
    }

    /**
     * Gets the panel associated with this component
     *
     * @return the panel owned by this component
     */
    public JPanel getPanel() {
        return panel;
    }

    /**
     * @param currentTime
     *            the current song time in microseconds
     */
    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
        panel.repaint();
    }

    /**
     * Sets the current sequence displayed by this panel
     *
     * @param sequence
     *            the parsed sequence to set
     */
    public void setCurrentSequence(ParsedSequence sequence) {
        this.sequence = sequence;
    }
}
