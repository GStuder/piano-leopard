package net.kreatious.pianoleopard.midi.track;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import net.kreatious.pianoleopard.midi.event.Event;
import net.kreatious.pianoleopard.midi.event.EventFactory;
import net.kreatious.pianoleopard.midi.event.TempoCache;

/**
 * Represents a parsed MIDI sequence containing multiple parsed tracks
 *
 * @author Jay-R Studer
 */
public class ParsedSequence {
    private final List<ParsedTrack> inactiveTracks = new CopyOnWriteArrayList<>();
    private final List<ParsedTrack> activeTracks = new CopyOnWriteArrayList<>();
    private final List<ParsedTrack> tracks;
    private final Sequence sequence;

    /**
     * Originally set to null to signify that the value has not been set -- this
     * is contrary to the normal expectations for an optional field
     */
    private Optional<File> file = null;

    private ParsedSequence(Sequence sequence, Track[] tracks, TempoCache cache) {
        this.sequence = sequence;
        this.tracks = Stream.of(tracks).map(track -> new ImmutableParsedTrack(track, cache)).collect(toList());
        activeTracks.addAll(this.tracks);
    }

    /**
     * Gets the file that this sequence was originally created from.
     * <p>
     * If the returned file is not empty, subsequent calls to this function are
     * guaranteed to return the same value.
     *
     * @return An optional containing the original file this sequence was
     *         created with.
     */
    public Optional<File> getFile() {
        return Optional.ofNullable(file).orElse(Optional.empty());
    }

    /**
     * Sets the file that this sequence was originally created from.
     * <p>
     * This setter may only be called once per sequence. Subsequent calls will
     * throw an exception.
     *
     * @param file
     *            the file for this sequence
     * @throws IllegalStateException
     *             if the file has already been set.
     */
    public void setFile(Optional<File> file) {
        if (this.file != null) {
            throw new IllegalStateException("Cannot set the file to " + file + "; already set to " + this.file);
        }
        this.file = file;
    }

    /**
     * Gets all tracks stored by this parsed MIDI sequence.
     *
     * @return a read only view of all parsed tracks contained in this sequence
     */
    public List<ParsedTrack> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    /**
     * Gets the active tracks stored by this parsed MIDI sequence.
     * <p>
     * Active tracks are those selected by the user for practice.
     *
     * @return a read only unordered view of the active parsed tracks contained
     *         in this sequence
     */
    public Collection<ParsedTrack> getActiveTracks() {
        return Collections.unmodifiableCollection(activeTracks);
    }

    /**
     * Gets the inactive tracks stored by this parsed MIDI sequence.
     * <p>
     * Inactive tracks are those not selected by the user for practice.
     *
     * @return a read only unordered view of the inactive parsed tracks
     *         contained in this sequence
     */
    public Collection<ParsedTrack> getInactiveTracks() {
        return Collections.unmodifiableCollection(inactiveTracks);
    }

    /**
     * Sets the specified track as active.
     * <p>
     * If the track is already active, no changes occur.
     *
     * @param track
     *            the parsed track in this sequence to modify
     * @param active
     *            true if the track should be active, otherwise false
     * @throws IllegalArgumentException
     *             if the track is not contained in this sequence
     */
    public void setTrackActive(ParsedTrack track, boolean active) {
        if (!tracks.contains(track)) {
            throw new IllegalArgumentException("Specified track is not contained by this container.");
        }

        if (active && inactiveTracks.contains(track)) {
            inactiveTracks.remove(track);
            activeTracks.add(track);
        } else if (!active && activeTracks.contains(track)) {
            activeTracks.remove(track);
            inactiveTracks.add(track);
        }
    }

    /**
     * Gets the original MIDI sequence used to create this parsed MIDI sequence.
     *
     * @return the original MIDI {@link Sequence}
     */
    public Sequence getSequence() {
        return sequence;
    }

    /**
     * Returns an empty parsed sequence containing nothing.
     *
     * @return a new empty {@link ParsedSequence}
     */
    public static ParsedSequence createEmpty() {
        try {
            final Sequence sequence = new Sequence(Sequence.SMPTE_25, 1);
            return new ParsedSequence(sequence, new Track[0], new TempoCache(sequence));
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
        if (sequence.getTracks().length != 1) {
            return new ParsedSequence(sequence, sequence.getTracks(), new TempoCache(sequence));
        }

        final Track[] tracks = IntStream.range(0, 16).mapToObj(x -> sequence.createTrack()).toArray(Track[]::new);
        final TempoCache cache = new TempoCache(sequence);

        final Track track = sequence.getTracks()[0];
        IntStream.range(0, track.size()).mapToObj(track::get).forEachOrdered(note -> {
            tracks[EventFactory.create(note, cache).map(Event::getChannel).orElse(0)].add(note);
        });
        sequence.deleteTrack(track);

        return new ParsedSequence(sequence, tracks, cache);
    }
}
