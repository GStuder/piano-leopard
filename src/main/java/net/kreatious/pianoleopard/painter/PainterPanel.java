package net.kreatious.pianoleopard.painter;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

import net.kreatious.pianoleopard.midi.ParsedSequence;
import net.kreatious.pianoleopard.midi.ParsedTrack;
import net.kreatious.pianoleopard.midi.sequencer.InputModel;
import net.kreatious.pianoleopard.midi.sequencer.OutputModel;

/**
 * Renders the currently playing sequence into a panel using double buffering.
 *
 * @author Jay-R Studer
 */
public class PainterPanel {
    private final class PainterPanelImpl extends JPanel {
        private static final long serialVersionUID = 3647951920113354307L;
        private final Painter painter = new Painter(new Dimension());

        PainterPanelImpl() {
            setPreferredSize(new Dimension(1000, 500));
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
     * Constructor declared private to prevent direct instantiation by
     * consumers.
     */
    private PainterPanel(ParsedTrack playedTrack) {
        this.playedTrack = playedTrack;
    }

    /**
     * Constructs a new {@link PainterPanel} connected to the specified track
     * containing events played by the user
     *
     * @param outputModel
     *            the output model for events sent to the synthesizer
     * @param inputModel
     *            the input model for events played by the user
     * @return a new instance of {@link PainterPanel}
     */
    public static JPanel create(OutputModel outputModel, InputModel inputModel) {
        final PainterPanel result = new PainterPanel(inputModel);
        outputModel.addCurrentTimeListener(result::setCurrentTime);
        outputModel.addStartListener(result::setCurrentSequence);
        return result.getPanel();
    }

    /**
     * Gets the panel associated with this component
     *
     * @return the panel owned by this component
     */
    private JPanel getPanel() {
        return panel;
    }

    /**
     * @param currentTime
     *            the current song time in microseconds
     */
    private void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
        panel.repaint();
    }

    /**
     * Sets the current sequence displayed by this panel
     *
     * @param sequence
     *            the parsed sequence to set
     */
    private void setCurrentSequence(ParsedSequence sequence) {
        this.sequence = sequence;
    }
}
