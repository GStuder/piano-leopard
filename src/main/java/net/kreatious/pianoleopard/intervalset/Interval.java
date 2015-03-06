package net.kreatious.pianoleopard.intervalset;

import java.util.Comparator;

class Interval implements Comparable<Interval> {
    private final long low;
    private final long high;

    Interval(long low, long high) {
        if (low > high) {
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
    boolean containsInterval(Interval interval) {
        return low <= interval.high && high >= interval.low;
    }

    long getLow() {
        return low;
    }

    long getHigh() {
        return high;
    }

    @Override
    public String toString() {
        return low + "-" + high;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(low) * 31 + Long.hashCode(high);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Interval)) {
            return false;
        } else {
            final Interval other = (Interval) obj;
            return low == other.low && high == other.high;
        }
    }

    @Override
    public int compareTo(Interval o) {
        return Comparator.comparing(Interval::getLow).thenComparing(Interval::getHigh).compare(this, o);
    }
}
