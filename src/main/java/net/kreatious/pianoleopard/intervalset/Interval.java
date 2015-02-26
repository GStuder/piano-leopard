package net.kreatious.pianoleopard.intervalset;

import java.util.Comparator;

class Interval<K extends Comparable<K>> implements Comparable<Interval<K>> {
    private final K low;
    private final K high;

    Interval(K low, K high) {
        if (low.compareTo(high) > 0) {
            throw new IllegalArgumentException("Range " + low + " -> " + high + " is invalid");
        }

        this.low = low;
        this.high = high;
    }

    /**
     * Tests if this interval overlaps a specified interval
     *
     * @param interval
     *            the interval to test for overlap
     * @return true if this interval overlaps the specified interval, otherwise
     *         false
     */
    boolean containsInterval(Interval<K> interval) {
        return low.compareTo(interval.high) <= 0 && high.compareTo(interval.low) >= 0;
    }

    K getLow() {
        return low;
    }

    K getHigh() {
        return high;
    }

    @Override
    public String toString() {
        return low + "-" + high;
    }

    @Override
    public int hashCode() {
        return low.hashCode() * 31 + high.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Interval)) {
            return false;
        } else {
            final Interval<?> other = (Interval<?>) obj;
            return low.equals(other.low) && high.equals(other.high);
        }
    }

    @Override
    public int compareTo(Interval<K> o) {
        return Comparator.<Interval<K>, K> comparing(Interval::getLow).thenComparing(Interval::getHigh)
                .compare(this, o);
    }
}
