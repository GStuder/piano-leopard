package net.kreatious.pianoleopard.midi;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

/**
 * Represents a parsed MIDI sequence containing multiple parsed tracks
 *
 * @author Jay-R Studer
 */
public class ParsedSequence {
    private final List<ParsedTrack> parsedTracks;

    private ParsedSequence(Track[] tracks, TempoCache cache) {
        parsedTracks = Collections.unmodifiableList(Stream.of(tracks).map(track -> new ParsedTrack(track, cache))
                .collect(toList()));
    }

    /**
     * Gets the tracks stored by this parsed MIDI sequence.
     *
     * @return a read only view of the parsed tracks contained in this sequence
     */
    public List<ParsedTrack> getTracks() {
        return parsedTracks;
    }

    /**
     * Returns an empty parsed sequence containing nothing.
     * 
     * @return a new empty {@link ParsedSequence}
     */
    public static ParsedSequence createEmpty() {
        try {
            return new ParsedSequence(new Track[0], new TempoCache(new Sequence(Sequence.SMPTE_25, 1)));
        } catch (final InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Parses a MIDI sequence, arranging it by tracks
     *
     * @param sequence
     *            the sequence to parse
     * @return a new {@link ParsedSequence}
     */
    public static ParsedSequence parseByTracks(Sequence sequence) {
        return new ParsedSequence(sequence.getTracks(), new TempoCache(sequence));
    }
}
