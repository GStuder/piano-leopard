package net.kreatious.pianoleopard;

import static net.kreatious.pianoleopard.keyboardselect.LightedKeyboardSelector.NAV_CHANNEL_PREFERENCE;

import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import net.kreatious.pianoleopard.Keys.KeyIterator;
import net.kreatious.pianoleopard.midi.ParsedSequence;
import net.kreatious.pianoleopard.midi.ParsedTrack;
import net.kreatious.pianoleopard.midi.event.Event;
import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.NoteEvent;
import net.kreatious.pianoleopard.midi.sequencer.InputModel;
import net.kreatious.pianoleopard.midi.sequencer.OutputModel;

/**
 * Provides support for lighted keyboards.
 * <p>
 * This sends MIDI messages to turn on keyboard lights slightly ahead of the
 * actual song, allowing the user time to react.
 *
 * @author Jay-R Studer
 */
class LightedKeyboardController {
    private static final long OFFSET = TimeUnit.MILLISECONDS.toMicros(500);
    private static final long NOTE_GAP = TimeUnit.MILLISECONDS.toMicros(50);

    private final OutputModel outputModel;
    private final Keys litKeys = new Keys();
    private final Keys keysToLight = new Keys();

    private volatile ParsedSequence sequence = ParsedSequence.createEmpty();
    private int navChannel;

    private LightedKeyboardController(OutputModel outputModel) {
        this.outputModel = outputModel;
    }

    /**
     * Constructs and starts a new instance of {@link LightedKeyboardController}
     * to control a lighted keyboard. After construction, keys in the current
     * sequence will light up slightly ahead of the actual song.
     *
     * @param preferences
     *            the preferences to listen for preference events
     * @param outputModel
     *            the {@link OutputModel} to send control events to
     * @param inputModel
     *            the {@link InputModel} to listen for user events
     * @return a new instance of {@link LightedKeyboardController}
     */
    static LightedKeyboardController create(Preferences preferences, OutputModel outputModel, InputModel inputModel) {
        final LightedKeyboardController result = new LightedKeyboardController(outputModel);
        preferences.addPreferenceChangeListener(e -> result.navChannel = e.getNode().getInt(NAV_CHANNEL_PREFERENCE, 3));
        result.navChannel = preferences.getInt(NAV_CHANNEL_PREFERENCE, 3);
        outputModel.addCurrentTimeListener(result::setCurrentTime);
        outputModel.addStartListener(result::setCurrentSequence);
        inputModel.addInputListener(result::onUserEvent);
        return result;
    }

    private void onUserEvent(Event event) {
        try {
            if (event instanceof NoteEvent == false) {
                return;
            }

            final int key = ((NoteEvent) event).getKey();
            if (event.isOn() && !litKeys.contains(key)) {
                // User pressed an unlit key
                outputModel.sendMessage(new ShortMessage(ShortMessage.NOTE_OFF, navChannel, key, 127));
            } else if (!event.isOn() && litKeys.contains(key)) {
                // User released a lit key
                outputModel.sendMessage(new ShortMessage(ShortMessage.NOTE_ON, navChannel, key, 1));
            }
        } catch (final InvalidMidiDataException e) {
            // Unreachable
            throw new IllegalStateException(e);
        }
    }

    private void setCurrentSequence(ParsedSequence sequence) {
        try {
            this.sequence = sequence;
            for (int key = 0; key != 128; key++) {
                if (litKeys.contains(key)) {
                    outputModel.sendMessage(new ShortMessage(ShortMessage.NOTE_ON, navChannel, key, 1));
                }
            }
        } catch (final InvalidMidiDataException e) {
            // Unreachable
            throw new IllegalStateException(e);
        }
    }

    private void setCurrentTime(long time) {
        try {
            updateKeysToLight(time);
            updateLitKeys();
        } catch (final InvalidMidiDataException e) {
            // Unreachable
            throw new IllegalStateException(e);
        }
    }

    private void updateKeysToLight(long time) {
        final long timePlusOffset = time + OFFSET;
        for (final ParsedTrack track : sequence.getActiveTracks()) {
            for (final EventPair<NoteEvent> note : track.getNotePairs(timePlusOffset - NOTE_GAP, timePlusOffset)) {
                if (note.getDuration() <= TimeUnit.MILLISECONDS.toMicros(10)) {
                    // Note too short
                    continue;
                } else if (timePlusOffset >= note.getOffTime() - NOTE_GAP) {
                    // Force a gap between notes
                    continue;
                }

                keysToLight.add(note.getOff().getKey());
            }
        }
    }

    private void updateLitKeys() throws InvalidMidiDataException {
        final KeyIterator litKeysIt = litKeys.iterator();
        while (litKeysIt.hasNext()) {
            final int key = litKeysIt.next();
            if (!keysToLight.contains(key)) {
                outputModel.sendMessage(new ShortMessage(ShortMessage.NOTE_OFF, navChannel, key, 127));
                litKeysIt.remove();
            }
        }

        final KeyIterator keysToLightIt = keysToLight.iterator();
        while (keysToLightIt.hasNext()) {
            final int key = keysToLightIt.next();
            if (!litKeys.contains(key)) {
                outputModel.sendMessage(new ShortMessage(ShortMessage.NOTE_ON, navChannel, key, 1));
                litKeys.add(key);
            }
        }
        keysToLight.clear();
    }
}
