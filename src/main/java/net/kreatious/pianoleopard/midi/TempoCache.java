package net.kreatious.pianoleopard.midi;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;

import com.google.common.annotations.VisibleForTesting;

/**
 * Maps from MIDI ticks to microseconds
 *
 * @author Jay-R Studer
 */
class TempoCache {
    private final int resolution;

    private final NavigableMap<Long, Integer> tempos;
    private final NavigableMap<Long, Long> microseconds = new TreeMap<>();

    /**
     * Constructs a new TempoCache with the specified MIDI Sequence
     *
     * @param sequence
     *            the {@link Sequence} to build a tempo cache for
     */
    public TempoCache(Sequence sequence) {
        resolution = sequence.getResolution();

        if (sequence.getDivisionType() != Sequence.PPQ) {
            // SMPTE time divisions are constant throughout
            tempos = new TreeMap<>(Collections.singletonMap(Long.MIN_VALUE,
                    (int) (TimeUnit.SECONDS.toMicros(1) / sequence.getDivisionType())));
            microseconds.put(0L, 0L);
            return;
        }

        // Effective tempo in microseconds per quarter note, keyed by MIDI ticks
        tempos = Stream.of(sequence.getTracks())
                .flatMap(track -> IntStream.range(0, track.size()).mapToObj(track::get))
                .filter(midiEvent -> midiEvent.getMessage().getStatus() == MetaMessage.META)
                .filter(midiEvent -> midiEvent.getMessage().getMessage()[1] == 0x51)
                .filter(midiEvent -> midiEvent.getMessage().getMessage()[2] == 0x03)
                .collect(collectingAndThen(toMap(MidiEvent::getTick, TempoCache::extractTempo, (key, value) -> {
                    throw new IllegalStateException("Duplicate key " + key);
                }, TreeMap::new), (Map<Long, Integer> map) -> (NavigableMap<Long, Integer>) map));

        // The default unspecified PPQ tempo is 0.5s per quarter note
        int previousTempo = 500000;
        tempos.putIfAbsent(0L, previousTempo);

        // Cache the elapsed microsecond durations keyed by MIDI event ticks
        long previousEventTick = 0;
        long elapsedMicroseconds = 0;
        for (final Entry<Long, Integer> tempo : tempos.entrySet()) {
            elapsedMicroseconds += (tempo.getKey() - previousEventTick) * previousTempo / resolution;
            previousEventTick = tempo.getKey();
            previousTempo = tempo.getValue();
            microseconds.put(tempo.getKey(), elapsedMicroseconds);
        }
    }

    /**
     * Extracts the new tempo from a Set Tempo message.
     * <p>
     * The Set Tempo message is formatted as {@code FF 51 03 xx xx xx}.
     * {@code FF} signifies a {@link MetaMessage#META META} event. {@code 51}
     * indicates it is a set tempo meta event. {@code 03} is the length of the
     * following byte array and is always 3. {@code xx xx xx} is the new tempo
     * in microseconds per quarter note, encoded as a 24-bit big endian integer.
     *
     * @param event
     *            the set tempo {@link MidiEvent} to extract new tempo from
     * @return the extracted tempo in microseconds per quarter note
     */
    @VisibleForTesting
    static int extractTempo(MidiEvent event) {
        // Read a 4 byte int that includes the 03 header, then remove the header
        return ByteBuffer.wrap(event.getMessage().getMessage()).getInt(2) & 0xFFFFFF;
    }

    /**
     * Converts a MIDI tick into elapsed microseconds
     *
     * @param ticks
     *            the MIDI ticks to convert into microseconds
     * @return the corresponding number of microseconds
     */
    long ticksToMicroseconds(long ticks) {
        final long previousEventTick = microseconds.floorKey(ticks);
        final long elapsedMicroseconds = microseconds.get(previousEventTick);
        final int currentTempo = tempos.floorEntry(ticks).getValue();

        return elapsedMicroseconds + (ticks - previousEventTick) * currentTempo / resolution;
    }
}
