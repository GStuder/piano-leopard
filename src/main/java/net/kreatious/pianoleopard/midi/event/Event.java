package net.kreatious.pianoleopard.midi.event;

import javax.sound.midi.ShortMessage;

/**
 * Represents a MIDI message associated with a particular channel and a
 * timestamp in microseconds.
 *
 * @author Jay-R Studer
 */
public abstract class Event {
    private final long time;
    private final int channel;

    /**
     * Constructs a new {@link Event} using the specified channel.
     *
     * @param channel
     *            the MIDI channel for this event between 0 and 15 inclusive.
     * @param time
     *            the time in microseconds when the message occurs.
     */
    Event(int channel, long time) {
        if (channel < 0 || channel > 15) {
            throw new IllegalArgumentException(channel + " is outside of the valid range [0, 15]");
        }

        this.channel = channel;
        this.time = time;
    }

    /**
     * Constructs a new {@link Event} using the specified {@link ShortMessage}.
     *
     * @param message
     *            the message to extract associated information from
     * @param time
     *            the time in microseconds when the message occurs.
     */
    Event(ShortMessage message, long time) {
        channel = message.getChannel();
        this.time = time;
    }

    /**
     * Gets the time in microseconds at which this event occurs.
     *
     * @return the time in microseconds when this event occurs.
     */
    public long getTime() {
        return time;
    }

    /**
     * Gets the channel on which this event occurs.
     * <p>
     * The channel value ranges from 0 to 15, inclusive.
     *
     * @return the channel on which this event occurs
     */
    public int getChannel() {
        return channel;
    }

    /**
     * Gets if this event is an off to on transition.
     * <p>
     * Note that an event can transition from off to on multiple times before an
     * on to off transition occurs.
     *
     * @return true if this event marks the start of an interval, false
     *         otherwise
     */
    public abstract boolean isOn();

    /**
     * Gets a slot that uniquely identifies which interval events belong to.
     * <p>
     * Equal slots are events occurring for the same channel and key.
     *
     * @return a uniquely identifying slot for this event
     */
    public abstract Slot getSlot();

    /**
     * Creates a new off event of the same type and slot with the specified
     * timestamp.
     *
     * @param offTime
     *            the off time of the created event measured in microseconds
     * @param <T>
     *            the type of the created event, must be the same as the
     *            declaring class.
     * @return a new event that is off but with the specified timestamp
     */
    public abstract <T extends Event> T createOff(long offTime);

    @Override
    public String toString() {
        return "Event[" + (isOn() ? "on" : "off") + ", channel: " + channel + ", slot: " + getSlot() + ", time: "
                + time + "]";
    }
}
