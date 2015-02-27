package net.kreatious.pianoleopard.midi.event;

/**
 * Represents a slot that uniquely identifies which interval events within the
 * same channel belong to. Intended to be used as a key for maps.
 *
 * @author Jay-R Studer
 */
public class Slot {
    private final int channel;
    private final Object key;

    Slot(int channel, Object key) {
        this.channel = channel;
        this.key = key;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Slot)) {
            return false;
        }
        final Slot other = (Slot) obj;
        return channel == other.channel && key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return 31 * channel + key.hashCode();
    }

    @Override
    public String toString() {
        return key.toString();
    }
}
