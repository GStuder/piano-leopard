package net.kreatious.pianoleopard.midi.event;

import javax.sound.midi.ShortMessage;

/**
 * Represents a note on or off event that is associated with a particular key
 * (note) number.
 *
 * @author Jay-R Studer
 */
public class NoteEvent extends Event {
    private final int key;
    private final int velocity;
    private final boolean on;
    private transient Slot slot;

    /**
     * Array of flags indicating if a raw MIDI key (note) modulus 12 is sharp or
     * not.
     * <p>
     * The first element corresponds to C, second is C#, third is D, etc.
     */
    private static final boolean[] SHARP_KEYS = { false, true, false, true, false, false, true, false, true, false,
            true, false };

    /**
     * Constructs a new {@link NoteEvent} with the specified data.
     * <p>
     * The channel will be 0 and velocity will be 127.
     *
     * @param key
     *            the raw MIDI key (note) for this event, between 0 and 127
     *            inclusive
     * @param on
     *            {@code true} if this event is a note on event, otherwise
     *            {@code false}
     * @param time
     *            the time that this event occurs, in microseconds
     */
    public NoteEvent(int key, boolean on, long time) {
        super(0, time);

        if (key < 0 || key > 127) {
            throw new IllegalArgumentException("Key " + key + " is out of range [0, 127]");
        }

        this.key = key;
        this.on = on;
        velocity = 127;
        slot = new Slot(getChannel(), key);
    }

    NoteEvent(ShortMessage message, long time) {
        super(message, time);

        key = message.getData1();
        velocity = message.getData2();
        slot = new Slot(message.getChannel(), key);

        if (message.getCommand() == ShortMessage.NOTE_OFF) {
            on = false;
        } else if (velocity == 0) {
            on = false;
        } else if (message.getCommand() == ShortMessage.NOTE_ON) {
            on = true;
        } else {
            throw new IllegalArgumentException("message " + message.getCommand() + " is not a note on/off message");
        }
    }

    static boolean canCreate(ShortMessage message) {
        return message.getCommand() == ShortMessage.NOTE_OFF || message.getCommand() == ShortMessage.NOTE_ON;
    }

    /**
     * Returns the note number associated with this event.
     * <p>
     * MIDI note numbers range from 0 to 127, inclusive. Note C4 is number 60.
     *
     * @return the raw key (note) number associated with this event
     */
    public int getKey() {
        return key;
    }

    /**
     * Returns the velocity associated with this event.
     * <p>
     * Velocity ranges from 0 to 127, inclusive.
     *
     * @return the velocity (loudness) associated with this event.
     */
    public int getVelocity() {
        return velocity;
    }

    /**
     * Returns if this note is considered sharp or not.
     *
     * @return {@code true} if this note event is a sharp note, {@code false}
     *         otherwise.
     */
    public boolean isSharp() {
        return SHARP_KEYS[key % 12];
    }

    @Override
    public boolean isOn() {
        return on;
    }

    @Override
    public Slot getSlot() {
        return slot;
    }
}
