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

    @Override
    public boolean isOn() {
        return on;
    }

    @Override
    public Slot getSlot() {
        return slot;
    }
}
