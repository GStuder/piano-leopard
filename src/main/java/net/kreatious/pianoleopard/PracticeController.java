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
        final JButton practiceButton = new JButton("Practice");
        outputModel.addOpenListener(sequence -> {
            updateText(practiceButton, sequence);
            practiceButton.setEnabled(true);
        });
        practiceButton.addActionListener(e -> outputModel.start());
        practiceButton.setEnabled(false);
        return practiceButton;
    }

    private static void updateText(JButton practiceButton, ParsedSequence sequence) {
        final Optional<String> fileName = sequence.getFile().map(File::getName);
        if (fileName.isPresent()) {
            practiceButton.setText("Practice " + fileName.get());
        } else {
            practiceButton.setText("Practice");
        }
    }
}
