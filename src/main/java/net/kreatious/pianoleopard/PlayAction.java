package net.kreatious.pianoleopard;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;

import net.kreatious.pianoleopard.midi.ParsedSequence;
import net.kreatious.pianoleopard.midi.sequencer.OutputModel;

/**
 * Provides the action for the play button.
 *
 * @author Jay-R Studer
 */
class PlayAction extends AbstractAction {
    private static final long serialVersionUID = 710373445454665603L;
    private final OutputModel outputModel;

    private PlayAction(OutputModel outputModel) {
        this.outputModel = outputModel;
    }

    static JButton create(OutputModel outputModel) {
        final PlayAction action = new PlayAction(outputModel);
        outputModel.addStartListener(action::setCurrentSequence);
        action.putValue(NAME, "Play");
        action.setEnabled(false);
        return new JButton(action);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        outputModel.start();
    }

    private void setCurrentSequence(ParsedSequence sequence) {
        putValue(NAME, "Play" + sequence.getFile().map(File::getName).map(name -> " " + name).orElse(""));
        setEnabled(true);
    }
}
