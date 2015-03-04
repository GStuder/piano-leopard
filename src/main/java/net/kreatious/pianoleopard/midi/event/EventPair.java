package net.kreatious.pianoleopard.midi.event;

/**
 * Represents an immutable pair of on/off events
 *
 * @author Jay-R Studer
 * @param <T>
 *            the subtype of Event held by this pair
 */
public class EventPair<T extends Event> {
    private final T on;
    private final T off;

    /**
     * Constructs a new {@link EventPair} with the specified pair of events
     *
     * @param on
     *            the on event
     * @param off
     *            the off event
     * @throws IllegalArgumentException
     *             if events do not represent a correct bounded pair
     */
    public EventPair(T on, T off) {
        if (!on.isOn()) {
            throw new IllegalArgumentException("on (" + on + ") was off");
        } else if (off.isOn()) {
            throw new IllegalArgumentException("off (" + off + ") was on");
        } else if (on.getChannel() != off.getChannel()) {
            throw new IllegalArgumentException("on (" + on + ") is not on the same channel as off (" + off + ")");
        } else if (!on.getSlot().equals(off.getSlot())) {
            throw new IllegalArgumentException("on (" + on + ") is not on the same slot as off (" + off + ")");
        } else if (on.getTime() > off.getTime()) {
            this.on = on;
            this.off = on.createOff(on.getTime());
            return;
        }

        this.on = on;
        this.off = off;
    }

    /**
     * @return the off event
     */
    public T getOff() {
        return off;
    }

    /**
     * @return the on event
     */
    public T getOn() {
        return on;
    }

    /**
     * @return the on time in microseconds
     */
    public long getOnTime() {
        return on.getTime();
    }

    /**
     * @return the off time in microseconds
     */
    public long getOffTime() {
        return off.getTime();
    }

    /**
     * @return the nonnegative duration of this event pair in microseconds
     */
    public long getDuration() {
        return off.getTime() - on.getTime();
    }

    /**
     * @return the slot of this event pair
     */
    public Object getSlot() {
        return on.getSlot();
    }

    /**
     * @return the channel of this event pair
     */
    public int getChannel() {
        return on.getChannel();
    }

    /**
     * Creates a new off event pair of the same type and slot with the specified
     * off timestamp.
     *
     * @param offTime
     *            the new off time measured in microseconds, value must be after
     *            the on time.
     * @return a new event pair with the specified timestamp
     */
    public EventPair<T> withOffTime(long offTime) {
        if (offTime < on.getTime()) {
            throw new IllegalArgumentException("The offtime must be on or after the on time");
        }
        return new EventPair<>(on, on.createOff(offTime));
    }

    @Override
    public String toString() {
        return on + ", " + off;
    }
}
