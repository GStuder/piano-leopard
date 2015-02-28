package net.kreatious.pianoleopard.painter.layout;

import static java.util.stream.Collectors.toMap;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Represents the relative corrections to apply in order to draw a note in the
 * same location as it would appear on a synthesizer.
 *
 * @author JStuder
 */
class NoteSize {
    private enum Pitch {
        C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP, A, A_SHARP, B;

        private static final Pitch[] VALUES = values();

        /**
         * Translates a MIDI key (note) value into a pitch
         *
         * @param midiNote
         *            the raw MIDI key (note) value to translate (0-127)
         * @return the corresponding {@link Pitch}
         */
        public static Pitch fromMidiNote(int midiNote) {
            return VALUES[midiNote % 12];
        }
    }

    private static final Map<Integer, NoteSize> NOTE_SIZES;
    static {
        final Map<Pitch, NoteSize> sizes = new EnumMap<>(Pitch.class);
        sizes.put(Pitch.C, forWhiteKey(-6f));
        sizes.put(Pitch.C_SHARP, forLeftBlackKey(-5f));
        sizes.put(Pitch.D, forWhiteKey(-5f));
        sizes.put(Pitch.D_SHARP, forRightBlackKey(-4f));
        sizes.put(Pitch.E, forWhiteKey(-4f));
        sizes.put(Pitch.F, forWhiteKey(-3f));
        sizes.put(Pitch.F_SHARP, forLeftBlackKey(-2f));
        sizes.put(Pitch.G, forWhiteKey(-2f));
        sizes.put(Pitch.G_SHARP, forCenterBlackKey(-1f));
        sizes.put(Pitch.A, forWhiteKey(-1f));
        sizes.put(Pitch.A_SHARP, forRightBlackKey(0f));
        sizes.put(Pitch.B, forWhiteKey(0f));

        NOTE_SIZES = IntStream.range(0, 128).boxed()
                .collect(toMap(i -> i, i -> new NoteSize(i, sizes.get(Pitch.fromMidiNote(i)))));
    }

    private static final float BLACK_WIDTH_FACTOR = 1.0f / 3.0f;
    private final float xOffsetFactor;
    private final float widthFactor;

    private NoteSize(float xOffsetFactor, float widthFactor) {
        this.xOffsetFactor = xOffsetFactor;
        this.widthFactor = widthFactor;
    }

    private NoteSize(int pitch, NoteSize keySize) {
        xOffsetFactor = (float) (keySize.xOffsetFactor + Math.floor(pitch / 12.0) * 7.0);
        widthFactor = keySize.widthFactor;
    }

    private static NoteSize forWhiteKey(float xAdjustment) {
        return new NoteSize(xAdjustment, 1.0f);
    }

    private static NoteSize forLeftBlackKey(float xAdjustment) {
        return new NoteSize(xAdjustment - BLACK_WIDTH_FACTOR * 2 / 3, BLACK_WIDTH_FACTOR);
    }

    private static NoteSize forCenterBlackKey(float xAdjustment) {
        return new NoteSize(xAdjustment - BLACK_WIDTH_FACTOR * 1 / 2, BLACK_WIDTH_FACTOR);
    }

    private static NoteSize forRightBlackKey(float xAdjustment) {
        return new NoteSize(xAdjustment - BLACK_WIDTH_FACTOR * 1 / 3, BLACK_WIDTH_FACTOR);
    }

    static NoteSize forPitch(int pitch) {
        return NOTE_SIZES.get(pitch);
    }

    /**
     * @return the normalized offset necessary to correctly position the note.
     */
    float getXOffsetFactor() {
        return xOffsetFactor;
    }

    /**
     * @return the factor to apply to the width of the note
     */
    float getWidthFactor() {
        return widthFactor;
    }
}
