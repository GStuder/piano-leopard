package net.kreatious.pianoleopard;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import net.kreatious.pianoleopard.midi.ParsedSequence;
import net.kreatious.pianoleopard.midi.ParsedTrack;
import net.kreatious.pianoleopard.midi.sequencer.OutputModel;

/**
 * View for altering which tracks are being practiced. Tracks being practiced
 * are muted.
 *
 * @author Jay-R Studer
 */
class PracticeTrackView {
    static Component create(OutputModel outputModel) {
        final JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        outputModel.addStartListener(new StartListener(panel));
        return panel;
    }

    /**
     * Reconstructs the panel's buttons when a new song is started.
     *
     * @author Jay-R Studer
     */
    private static final class StartListener implements Consumer<ParsedSequence> {
        private final JPanel panel;
        private static final Predicate<ParsedTrack> CONTAINS_NOTES = track -> track.getNotePairs(0, Long.MAX_VALUE)
                .iterator().hasNext();

        private StartListener(JPanel panel) {
            this.panel = panel;
        }

        @Override
        public void accept(ParsedSequence sequence) {
            panel.removeAll();
            panel.add(new JLabel("Tracks:"));
            sequence.getTracks().stream().filter(CONTAINS_NOTES).forEach(track -> {
                panel.add(createButton(sequence, track));
            });
            panel.revalidate();
        }

        private static Component createButton(ParsedSequence sequence, ParsedTrack track) {
            final JToggleButton button = new JToggleButton();
            button.setMargin(new Insets(8, 8, 8, 8));
            button.addItemListener(e -> sequence.setTrackActive(track, e.getStateChange() == ItemEvent.SELECTED));
            button.setSelected(sequence.getActiveTracks().contains(track));
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    sequence.setTrackActive(track, !button.isSelected());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    sequence.setTrackActive(track, button.isSelected());
                }
            });
            return button;
        }
    }
}
