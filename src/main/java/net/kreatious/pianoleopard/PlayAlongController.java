package net.kreatious.pianoleopard;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.sound.midi.MidiMessage;
import javax.swing.JToggleButton;

import net.kreatious.pianoleopard.midi.event.Event;
import net.kreatious.pianoleopard.midi.sequencer.OutputModel;
import net.kreatious.pianoleopard.midi.sequencer.OutputModel.EventAction;

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

        final PlayAlongEventHandler eventHandler = new PlayAlongEventHandler();
        button.addItemListener(e -> eventHandler.playAlong = e.getStateChange() == ItemEvent.SELECTED);
        button.addMouseListener(new ToggleListener(toggle -> eventHandler.playAlong = toggle ^ button.isSelected()));
        outputModel.addEventHandler(eventHandler);

        button.setVisible(false);
        outputModel.addOpenListener(sequence -> button.setVisible(true));
        return button;
    }

    private static class PlayAlongEventHandler implements BiFunction<MidiMessage, Optional<Event>, EventAction> {
        boolean playAlong;

        @Override
        public EventAction apply(MidiMessage message, Optional<Event> event) {
            if (!event.isPresent()) {
                return EventAction.UNHANDLED;
            } else if (playAlong) {
                return EventAction.PLAY;
            } else {
                return EventAction.UNHANDLED;
            }
        }
    }
}
