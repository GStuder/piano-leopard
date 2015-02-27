package net.kreatious.pianoleopard.midi.event;

import java.util.Optional;

/**
 * Represents the various foot pedal
 *
 * @author Jay-R Studer
 */
public enum Pedal {
    /**
     * Sustain pedal.
     * <p>
     * While this pedal is pressed, played notes decay slowly. This sounds as if
     * note off messages are ignored.
     */
    SUSTAIN(64),

    /**
     * Sostenuto pedal.
     * <p>
     * When this pedal is pressed, only the notes currently being played are
     * sustained. Commonly abbreviated as S.P.
     */
    SOSTENUTO(65),

    /**
     * Portamento pedal.
     * <p>
     * While this pedal is pressed, notes glide continuously in pitch from one
     * to the next.
     */
    PORTAMENTO(66),

    /**
     * Soft pedal.
     * <p>
     * While this pedal is pressed, notes are much quieter.
     */
    SOFT(67);

    private final int data;

    private Pedal(int data) {
        this.data = data;
    }

    /**
     * Returns the raw MIDI data value associated with a control change message.
     * 
     * @return the data1 value for a control change message using this pedal
     */
    public int getData() {
        return data;
    }

    /**
     * Determines the pedal enum associated with a MIDI data value.
     *
     * @param data
     *            the MIDI data1 value associated with a control change message
     * @return an optional containing the pedal associated with the data, or
     *         empty
     */
    public static Optional<Pedal> lookup(int data) {
        switch (data) {
        case 64:
            return Optional.of(SUSTAIN);
        case 65:
            return Optional.of(PORTAMENTO);
        case 66:
            return Optional.of(SOSTENUTO);
        case 67:
            return Optional.of(SOFT);
        default:
            return Optional.empty();
        }
    }
}
