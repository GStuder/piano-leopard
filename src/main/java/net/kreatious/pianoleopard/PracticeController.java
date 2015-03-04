package net.kreatious.pianoleopard;

import java.awt.Component;
import java.io.File;
import java.util.Optional;

import javax.swing.JButton;

import net.kreatious.pianoleopard.midi.ParsedSequence;
import net.kreatious.pianoleopard.midi.sequencer.OutputModel;

/**
 * Provides the controller for the practice action.
 *
 * @author Jay-R Studer
 */
class PracticeController {
    private PracticeController() {
    }

    /**
     * Constructs a view and associates it with its controller
     */
    static Component create(OutputModel outputModel) {
        final JButton button = new JButton("Practice");
        outputModel.addStartListener(sequence -> updateText(button, sequence));
        outputModel.addStartListener(sequence -> button.setEnabled(true));
        button.addActionListener(e -> outputModel.start());
        button.setEnabled(false);
        return button;
    }

    private static void updateText(JButton button, ParsedSequence sequence) {
        final Optional<String> fileName = sequence.getFile().map(File::getName);
        if (fileName.isPresent()) {
            button.setText("Practice " + fileName.get());
        } else {
            button.setText("Practice");
        }
    }
}
