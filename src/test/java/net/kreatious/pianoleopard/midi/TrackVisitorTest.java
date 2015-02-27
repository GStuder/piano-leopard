package net.kreatious.pianoleopard.midi;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.describedAs;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import net.kreatious.pianoleopard.midi.event.Event;
import net.kreatious.pianoleopard.midi.event.EventPair;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Test;
import org.mockito.InOrder;

/**
 * Tests for {@link TrackVisitor}
 *
 * @author Jay-R Studer
 */
@SuppressWarnings("javadoc")
public class TrackVisitorTest {
    private final TempoCache cache;
    private final Sequence sequence;
    private final Track track;
    private final TrackVisitor visitor = spy(TrackVisitor.class);
    private final InOrder order = inOrder(visitor);
    private final Map<Integer, MidiEventBuilder> expectedValues = new HashMap<>();

    /**
     * Constructs a new {@link TrackVisitorTest}...
     *
     * @throws InvalidMidiDataException
     *             this test does not throw this exception
     */
    public TrackVisitorTest() throws InvalidMidiDataException {
        sequence = new Sequence(Sequence.SMPTE_25, 10);
        cache = new TempoCache(sequence);
        track = sequence.createTrack();
    }

    @Test
    public void testOneNote() throws InvalidMidiDataException {
        noteOn().addId(0);
        noteOff().addId(1);

        visitor.accept(track, cache);

        order.verify(visitor).visitEventPair(ofIds(0, 1));
    }

    @Test
    public void testTwoNotes() throws InvalidMidiDataException {
        noteOn().addId(0);
        noteOff().addId(1);
        noteOn().addId(2);
        noteOff().addId(3);

        visitor.accept(track, cache);

        order.verify(visitor).visitEventPair(ofIds(0, 1));
        order.verify(visitor).visitEventPair(ofIds(2, 3));
    }

    @Test
    public void testTwoNotesOnDifferentChannelsSameTime() throws InvalidMidiDataException {
        noteOn().channel(0).tick(0).addId(0);
        noteOff().channel(0).tick(1).addId(1);
        noteOn().channel(1).tick(0).addId(2);
        noteOff().channel(1).tick(1).addId(3);

        visitor.accept(track, cache);

        order.verify(visitor).visitEventPair(ofIds(0, 1));
        order.verify(visitor).visitEventPair(ofIds(2, 3));
    }

    @Test
    public void testTwoNotesOnDifferentKeysSameTime() throws InvalidMidiDataException {
        noteOn().key(0).tick(0).addId(0);
        noteOff().key(0).tick(1).addId(1);
        noteOn().key(1).tick(0).addId(2);
        noteOff().key(1).tick(1).addId(3);

        visitor.accept(track, cache);

        order.verify(visitor).visitEventPair(ofIds(0, 1));
        order.verify(visitor).visitEventPair(ofIds(2, 3));
    }

    @Test
    public void testIncompletePreviousNote() throws InvalidMidiDataException {
        noteOn().addId(0);
        noteOn().addId(1);
        noteOn().addId(2);
        noteOff().addId(3);

        visitor.accept(track, cache);

        order.verify(visitor).visitEventPair(ofIds(2, 3));
    }

    @Test
    public void testIgnoreRedundantNoteOff() throws InvalidMidiDataException {
        noteOn().addId(0);
        noteOff().addId(1);
        noteOff().addId(2);
        noteOff().addId(3);

        visitor.accept(track, cache);

        order.verify(visitor).visitEventPair(ofIds(0, 1));
    }

    @Test
    public void testExtraNoteOnAtSameTime() throws InvalidMidiDataException {
        noteOn().tick(0).addId(0);
        noteOn().tick(0).addId(1);
        noteOff().addId(2);

        visitor.accept(track, cache);

        order.verify(visitor).visitEventPair(ofIds(0, 2));
    }

    @Test
    public void testExtraNoteOffAtSameTime() throws InvalidMidiDataException {
        noteOn().addId(0);
        noteOff().tick(5).addId(1);
        noteOff().tick(5).addId(2);

        visitor.accept(track, cache);

        order.verify(visitor).visitEventPair(ofIds(0, 1));
    }

    @Test
    public void testZeroDurationNote() throws InvalidMidiDataException {
        noteOn().tick(0).addId(0);
        noteOff().tick(0).addId(1);

        visitor.accept(track, cache);

        order.verify(visitor).visitEventPair(ofIds(0, 1));
    }

    @Test
    public void testIgnoreOffsWithNoOnNote() throws InvalidMidiDataException {
        noteOff().addId(0);
        noteOn().addId(1);
        noteOff().addId(2);

        visitor.accept(track, cache);

        order.verify(visitor).visitEventPair(ofIds(1, 2));
    }

    @Test
    public void testIgnoreOnsWithNoOffNote() throws InvalidMidiDataException {
        noteOn().addId(0);
        noteOff().addId(1);
        noteOn().addId(2);

        visitor.accept(track, cache);

        order.verify(visitor).visitEventPair(ofIds(0, 1));
    }

    /**
     * Checks that there are no extra unverified visits
     */
    @After
    public void noExtraVisits() {
        order.verify(visitor, never()).visitEventPair(any());
    }

    private EventPair<Event> ofIds(int onId, int offId) {
        return argThat(allOf(hasProperty("on", expectedValues.get(onId).matcher()),
                hasProperty("off", expectedValues.get(offId).matcher())));
    }

    private MidiEventBuilder noteOn() {
        return new MidiEventBuilder(ShortMessage.NOTE_ON, 127);
    }

    private MidiEventBuilder noteOff() {
        return new MidiEventBuilder(ShortMessage.NOTE_OFF, 0);
    }

    /**
     * Builder for adding MidiEvents to the test track.
     * <p>
     * Defaults to channel 0, middle C, 1 tick after the last event
     *
     * @author Jay-R Studer
     */
    private class MidiEventBuilder {
        private final int command;
        private int channel;
        private int key = 60;
        private final int velocity;
        private long tick = track.ticks() + 1;

        MidiEventBuilder(int command, int velocity) {
            this.command = command;
            this.velocity = velocity;
        }

        MidiEventBuilder channel(int value) {
            channel = value;
            return this;
        }

        MidiEventBuilder key(int value) {
            key = value;
            return this;
        }

        MidiEventBuilder tick(long value) {
            tick = value;
            return this;
        }

        void addId(int id) throws InvalidMidiDataException {
            track.add(new MidiEvent(new ShortMessage(command, channel, key, velocity), tick));
            assertThat("No duplicate note ids", expectedValues.containsKey(id), is(false));
            expectedValues.put(id, this);
        }

        Matcher<Event> matcher() {
            final long time = cache.ticksToMicroseconds(tick);
            final Matcher<Event> propertyMatcher = allOf(hasProperty("key", is(key)),
                    hasProperty("velocity", is(velocity)), hasProperty("time", is(time)),
                    hasProperty("channel", is(channel)));
            return describedAs("[%0, channel: %1, time: %2]", propertyMatcher, velocity >= 64 ? "on" : "off", channel,
                    time);
        }
    }
}
