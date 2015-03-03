package net.kreatious.pianoleopard.midi.sequencer;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.describedAs;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.function.LongConsumer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import net.kreatious.pianoleopard.midi.event.EventPair;
import net.kreatious.pianoleopard.midi.event.NoteEvent;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Tests for {@link InputModel}
 *
 * @author Jay-R Studer
 */
public class InputModelTest {
    private static final int CHANNEL = 0;

    private final Transmitter transmitter = mock(Transmitter.class);
    private final MidiDevice input = given(mock(MidiDevice.class).getTransmitter()).willReturn(transmitter).getMock();

    private final OutputModel outputModel = mock(OutputModel.class);
    private final InputModel inputModel = InputModel.create(input, outputModel);
    private final Receiver receiver;

    private long currentTime;
    private final LongConsumer currentTimeListener;

    /**
     * Constructs a new {@link InputModelTest} by initializing fields to the
     * listeners used by the input model.
     *
     * @throws MidiUnavailableException
     *             exception is never thrown by this test.
     */
    public InputModelTest() throws MidiUnavailableException {
        final ArgumentCaptor<Receiver> receiverCaptor = ArgumentCaptor.forClass(Receiver.class);
        then(transmitter).should().setReceiver(receiverCaptor.capture());
        receiver = receiverCaptor.getValue();

        final ArgumentCaptor<LongConsumer> currentTimeCaptor = ArgumentCaptor.forClass(LongConsumer.class);
        then(outputModel).should().addCurrentTimeListener(currentTimeCaptor.capture());
        currentTimeListener = currentTimeCaptor.getValue();
    }

    /**
     * Tests input model when the user presses and releases a single note
     */
    @Test
    public void testFullNote() {
        pressNote(60);
        timeAdvancesBy(1);
        releaseNote(60);
        timeAdvancesBy(1);

        assertThat(inputModel.getNotePairs(0, 2), contains(noteWithTime(60, 0, 1)));
    }

    /**
     * Tests input model when the user presses and releases a chord
     */
    @Test
    public void testFullChord() {
        pressNote(60);
        pressNote(61);
        timeAdvancesBy(1);
        releaseNote(60);
        releaseNote(61);
        timeAdvancesBy(1);

        assertThat(inputModel.getNotePairs(0, 2), hasItem(noteWithTime(60, 0, 1)));
        assertThat(inputModel.getNotePairs(0, 2), hasItem(noteWithTime(61, 0, 1)));
        assertThat(inputModel.getNotePairs(0, 2), iterableWithSize(2));
    }

    /**
     * Tests input model when the user presses, but doesn't release, a single
     * note. Doesn't wait.
     */
    @Test
    public void testPressedNote() {
        pressNote(60);

        assertThat(inputModel.getNotePairs(0, 1), contains(noteWithTime(60, 0, 0)));
    }

    /**
     * Tests input model when the user presses, but doesn't release, a single
     * note. Waits for 1 microsecond.
     */
    @Test
    public void testPressedNoteZeroTick() {
        pressNote(60);
        timeAdvancesBy(0);

        assertThat(inputModel.getNotePairs(0, 1), contains(noteWithTime(60, 0, 0)));
    }

    /**
     * Tests input model when the user presses, but doesn't release, a single
     * note
     */
    @Test
    public void testPressedNoteOneTick() {
        pressNote(60);
        timeAdvancesBy(1);

        assertThat(inputModel.getNotePairs(0, 1), contains(noteWithTime(60, 0, 1)));
    }

    /**
     * Tests input model when the user presses, but doesn't release, a single
     * note, for two ticks.
     */
    @Test
    public void testPressedNoteTwoTicks() {
        pressNote(60);
        timeAdvancesBy(1);
        timeAdvancesBy(1);

        assertThat(inputModel.getNotePairs(0, 1), contains(noteWithTime(60, 0, 2)));
    }

    private void pressNote(int key) {
        try {
            receiver.send(new ShortMessage(ShortMessage.NOTE_ON, CHANNEL, key, 127), -1);
        } catch (final InvalidMidiDataException e) {
            throw new IllegalArgumentException(key + " must be between 0 and 127", e);
        }
    }

    private void timeAdvancesBy(long time) {
        currentTime += time;
        currentTimeListener.accept(currentTime);
    }

    private void releaseNote(int key) {
        try {
            receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, CHANNEL, key, 127), -1);
        } catch (final InvalidMidiDataException e) {
            throw new IllegalArgumentException(key + " must be between 0 and 127", e);
        }
    }

    private static Matcher<? super EventPair<NoteEvent>> noteWithTime(int key, long onTime, long offTime) {
        final Matcher<EventPair<NoteEvent>> pairMatcher = allOf(hasProperty("off", hasProperty("key", is(key))),
                hasProperty("onTime", is(onTime)), hasProperty("offTime", is(offTime)),
                hasProperty("channel", is(CHANNEL)));
        return describedAs("Note[key: %0, on: %1, off: %2]", pairMatcher, key, onTime, offTime);
    }
}
