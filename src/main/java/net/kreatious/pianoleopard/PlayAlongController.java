package net.kreatious.pianoleopard;

import java.awt.Component;
import java.awt.event.ItemEvent;

import javax.swing.JToggleButton;

import net.kreatious.pianoleopard.midi.sequencer.OutputModel;

/**
 * Provides the controller for the play along action.
 * <p>
 * When playing along, practiced tracks are not muted.
 *
 * @author Jay-R Studer
 */
class PlayAlongController {
    private PlayAlongController() {
    }

    /**
     * Constructs a view and associates it with its controller
     */
    static Component create(OutputModel outputModel) {
        final JToggleButton button = new JToggleButton("Play along");
        button.setEnabled(false);
        outputModel.addPlayListener(() -> button.setEnabled(true));
        button.addItemListener(e -> outputModel.setPlayAlong(e.getStateChange() == ItemEvent.SELECTED));
        button.addMouseListener(new ToggleListener(toggle -> outputModel.setPlayAlong(toggle ^ button.isSelected())));
        return button;
    }
}
