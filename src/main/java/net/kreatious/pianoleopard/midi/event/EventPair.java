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
        } else if (on.getTime() > off.getTime()) {
            throw new IllegalArgumentException("on (" + on + ") was after off (" + off + ")");
        } else if (on.getChannel() != off.getChannel()) {
            throw new IllegalArgumentException("on (" + on + ") is not on the same channel as off (" + off + ")");
        } else if (!on.getSlot().equals(off.getSlot())) {
            throw new IllegalArgumentException("on (" + on + ") is not on the same slot as off (" + off + ")");
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

    @Override
    public String toString() {
        return on + ", " + off;
    }
}
