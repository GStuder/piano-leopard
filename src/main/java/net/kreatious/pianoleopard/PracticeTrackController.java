package net.kreatious.pianoleopard;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.sound.midi.MidiMessage;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import net.kreatious.pianoleopard.midi.ParsedSequence;
import net.kreatious.pianoleopard.midi.ParsedTrack;
import net.kreatious.pianoleopard.midi.event.Event;
import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.NoteEvent;
import net.kreatious.pianoleopard.midi.event.PedalEvent;
import net.kreatious.pianoleopard.midi.event.Slot;
import net.kreatious.pianoleopard.midi.sequencer.OutputModel;
import net.kreatious.pianoleopard.midi.sequencer.OutputModel.EventAction;

/**
 * Provides the controller for altering which tracks are being practiced.
 *
 * @author Jay-R Studer
 */
class PracticeTrackController {
    private PracticeTrackController() {
    }

    /**
     * Constructs a view and associates it with its controller
     */
    static Component create(OutputModel outputModel) {
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        outputModel.addOpenListener(new OpenListener(panel));
        PracticeTrackEventHandler.create(outputModel);
        return panel;
    }

    /**
     * Reconstructs the panel's buttons when a new song is opened.
     *
     * @author Jay-R Studer
     */
    private static final class OpenListener implements Consumer<ParsedSequence> {
        private final JPanel panel;
        private static final Predicate<ParsedTrack> CONTAINS_NOTES = track -> track.getNotePairs(0, Long.MAX_VALUE)
                .iterator().hasNext();

        private OpenListener(JPanel panel) {
            this.panel = panel;
        }

        @Override
        public void accept(ParsedSequence sequence) {
            panel.removeAll();
            panel.add(new JLabel("Tracks:"));
            sequence.getTracks().stream().filter(CONTAINS_NOTES)
                    .forEach(track -> panel.add(createButton(sequence, track)));
            panel.revalidate();
        }

        private static Component createButton(ParsedSequence sequence, ParsedTrack track) {
            final JToggleButton button = new JToggleButton();
            button.setMargin(new Insets(8, 8, 8, 8));
            button.addItemListener(e -> sequence.setTrackActive(track, e.getStateChange() == ItemEvent.SELECTED));
            button.setSelected(sequence.getActiveTracks().contains(track));
            button.addMouseListener(new ToggleListener(toggle -> sequence.setTrackActive(track, button.isSelected()
                    ^ toggle)));
            return button;
        }
    }

    private static final class PracticeTrackEventHandler implements
            BiFunction<MidiMessage, Optional<Event>, EventAction> {
        private static final long TOLERANCE = TimeUnit.SECONDS.toMicros(2);
        private ParsedSequence sequence = ParsedSequence.createEmpty();

        static void create(OutputModel outputModel) {
            final PracticeTrackEventHandler eventHandler = new PracticeTrackEventHandler();
            outputModel.addOpenListener(sequence -> eventHandler.sequence = sequence);
            outputModel.addEventHandler(eventHandler);
        }

        /**
         * An event is considered muted if its {@link Slot} is not found in any
         * inactive tracks at the current time.
         */
        @Override
        public EventAction apply(MidiMessage message, Optional<Event> optionalEvent) {
            if (!optionalEvent.isPresent()) {
                return EventAction.UNHANDLED;
            }

            final Event event = optionalEvent.get();
            if (!event.isOn()) {
                // Never mute a note off event
                return EventAction.PLAY;
            }

            for (final ParsedTrack track : sequence.getInactiveTracks()) {
                final Iterable<? extends EventPair<? extends Event>> pairs;
                if (event instanceof NoteEvent) {
                    pairs = track.getNotePairs(event.getTime(), event.getTime() + TOLERANCE);
                } else if (event instanceof PedalEvent) {
                    pairs = track.getPedalPairs(event.getTime(), event.getTime() + TOLERANCE);
                } else {
                    return EventAction.UNHANDLED;
                }

                for (final EventPair<? extends Event> pair : pairs) {
                    if (pair.getSlot().equals(event.getSlot())) {
                        return EventAction.PLAY;
                    }
                }
            }
            return EventAction.MUTE;
        }
    }
}
