package net.kreatious.pianoleopard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;

import net.kreatious.pianoleopard.midi.ParsedSequence;
import net.kreatious.pianoleopard.midi.sequencer.OutputModel;

/**
 * Provides the action for the practice button.
 *
 * @author Jay-R Studer
 */
class PracticeAction extends AbstractAction {
    private static final long serialVersionUID = 710373445454665603L;
    private final OutputModel outputModel;

    private PracticeAction(OutputModel outputModel) {
        this.outputModel = outputModel;
    }

    static Component create(OutputModel outputModel) {
        final PracticeAction action = new PracticeAction(outputModel);
        outputModel.addStartListener(action::setCurrentSequence);
        action.putValue(NAME, "Practice");
        action.setEnabled(false);
        return new JButton(action);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        outputModel.start();
    }

    private void setCurrentSequence(ParsedSequence sequence) {
        putValue(NAME, "Practice" + sequence.getFile().map(File::getName).map(name -> " " + name).orElse(""));
        setEnabled(true);
    }
}
