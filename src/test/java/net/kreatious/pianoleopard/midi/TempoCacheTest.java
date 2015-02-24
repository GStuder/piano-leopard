package net.kreatious.pianoleopard.midi;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.nio.ByteBuffer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for {@link TempoCache}
 *
 * @author Jay-R Studer
 */
@RunWith(Enclosed.class)
public class TempoCacheTest {
    /**
     * Tests for {@link TempoCache#extractTempo(MidiEvent)}
     */
    @RunWith(Parameterized.class)
    public static class GetTempoTest {
        /**
         * A byte array representing a potential SET_TEMPO MIDI Message
         */
        @Parameter(0)
        public byte[] message;

        /**
         * The expected internal representation of the message
         */
        @Parameter(1)
        public int expected;

        /**
         * @return an array of tests to try
         */
        @Parameters(name = "{1}")
        public static Object[][] parameters() {
            // First 3 bytes ignored by extract tempo. Normally it's FF 51 03
            return new Object[][] { { new byte[] { 8, 8, 8, 1, 0, 0 }, 0x010000 },
                    { new byte[] { 8, 8, 8, 127, 0, 0 }, 0x7F0000 },
                    { new byte[] { 8, 8, 8, (byte) 128, 0, 0 }, 0x800000 },
                    { new byte[] { 8, 8, 8, (byte) 255, 0, 0 }, 0xFF0000 },
                    { new byte[] { 8, 8, 8, 0, 127, 0 }, 0x7F00 },
                    { new byte[] { 8, 8, 8, 0, (byte) 128, 0 }, 0x8000 },
                    { new byte[] { 8, 8, 8, 0, (byte) 255, 0 }, 0xFF00 },
                    { new byte[] { 8, 8, 8, 0, 0, 1 }, 0x00000001 },
                    { new byte[] { 8, 8, 8, 0, 0, (byte) 128 }, 0x000080 },
                    { new byte[] { 8, 8, 8, 0, 0, (byte) 255 }, 0xFF }, };
        }

        /**
         * Tests for {@link TempoCache#extractTempo(MidiEvent)}
         */
        @Test
        public void testGetTempo() {
            final MidiMessage midiMessage = given(mock(MidiMessage.class).getMessage()).willReturn(message).getMock();
            final MidiEvent event = given(mock(MidiEvent.class).getMessage()).willReturn(midiMessage).getMock();

            assertThat(TempoCache.extractTempo(event), is(expected));
        }
    }

    /**
     * Unit tests for {@link TempoCache#ticksToMicroseconds(long)}
     */
    public static class TicksToMicrosTest {
        /**
         * Tests {@link TempoCache#ticksToMicroseconds(long)} for PPQ (Pulses
         * per quarter note) sequences.
         *
         * @throws InvalidMidiDataException
         *             This exception is never thrown by this test
         */
        @Test
        public void testTicks2MicrosPpq() throws InvalidMidiDataException {
            // Note: A reference implementation of TempoCache is available at
            // com.sun.media.sound.MidiUtils.TempoCache, but is an internal
            // implementation detail of the MIDI library and may not be present
            // in all runtime environments.

            final Sequence sequence = new Sequence(Sequence.PPQ, 2);
            final Track track = sequence.createTrack();
            createSetTempoEvent(track, 0, 500);
            createSetTempoEvent(track, 1000, 50000);
            createSetTempoEvent(track, 5000, 75000);
            createSetTempoEvent(track, 10000, 5000);

            final TempoCache cache = new TempoCache(sequence);
            assertThat(cache.ticksToMicroseconds(0), is(0L));
            assertThat(cache.ticksToMicroseconds(1000), is(250_000L));
            assertThat(cache.ticksToMicroseconds(3000), is(50_250_000L));
            assertThat(cache.ticksToMicroseconds(5000), is(100_250_000L));
            assertThat(cache.ticksToMicroseconds(7000), is(175_250_000L));
            assertThat(cache.ticksToMicroseconds(10000), is(287_750_000L));
            assertThat(cache.ticksToMicroseconds(13000), is(295_250_000L));
        }

        /**
         * Tests {@link TempoCache#ticksToMicroseconds(long)} for SMPTE_25
         * sequences with a resolution of 4.
         * <p>
         * SMPTE sequences ignore SET_TEMPO events, but do depend on the
         * resolution.
         *
         * @throws InvalidMidiDataException
         *             This exception is never thrown by this test
         */
        @Test
        public void testTicks2MicrosSmpte25() throws InvalidMidiDataException {
            final Sequence sequence = new Sequence(Sequence.SMPTE_25, 4);
            final Track track = sequence.createTrack();
            createSetTempoEvent(track, 0, 1000);
            createSetTempoEvent(track, 1000, 2000);

            final TempoCache cache = new TempoCache(sequence);
            assertThat(cache.ticksToMicroseconds(0), is(0L));
            assertThat(cache.ticksToMicroseconds(500), is(5_000_000L));
            assertThat(cache.ticksToMicroseconds(1000), is(10_000_000L));
            assertThat(cache.ticksToMicroseconds(1500), is(15_000_000L));
        }

        /**
         * Tests {@link TempoCache#ticksToMicroseconds(long)} for SMPTE_30DROP
         * sequences with a resolution of 5.
         *
         * @throws InvalidMidiDataException
         *             This exception is never thrown by this test
         */
        @Test
        public void testTicks2MicrosSmpte30Drop() throws InvalidMidiDataException {
            final Sequence sequence = new Sequence(Sequence.SMPTE_30DROP, 5);
            final Track track = sequence.createTrack();
            createSetTempoEvent(track, 0, 1000);
            createSetTempoEvent(track, 1000, 2000);

            final TempoCache cache = new TempoCache(sequence);
            assertThat(cache.ticksToMicroseconds(0), is(0L));
            assertThat(cache.ticksToMicroseconds(500), is(3_336_600L));
            assertThat(cache.ticksToMicroseconds(1000), is(6_673_200L));
            assertThat(cache.ticksToMicroseconds(1500), is(10_009_800L));
        }

        private static void createSetTempoEvent(Track track, long tick, int tempo) throws InvalidMidiDataException {
            track.add(new MidiEvent(new MetaMessage(0x51, ByteBuffer.allocate(4).putInt(tempo << 8).array(), 3), tick));
        }
    }
}
