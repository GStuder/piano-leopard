package net.kreatious.pianoleopard.intervalset;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;

class RangeIterable<V> implements Iterable<V> {
    private final IntervalSet<V> set;
    private final Interval range;
    private final Optional<Entry<V>> first;

    RangeIterable(IntervalSet<V> intervalSet, long lowKey, long highKey) {
        set = intervalSet;
        range = new Interval(lowKey, highKey);
        first = first();
    }

    @Override
    public Iterator<V> iterator() {
        return new RangeIterator<>(set, range, first);
    }

    private Optional<Entry<V>> first() {
        final Predicate<Entry<V>> withinResult = current -> current.getKey().containsInterval(range);
        final Predicate<Entry<V>> maximumIsAfterRangeStart = current -> current.getMaximum() >= range.getLow();

        Optional<Entry<V>> current = set.getRoot();
        while (true) {
            if (current.flatMap(Entry::getLeft).filter(maximumIsAfterRangeStart).isPresent()) {
                current = current.flatMap(Entry::getLeft);
            } else if (current.filter(withinResult).isPresent()) {
                return current;
            } else if (current.flatMap(Entry::getRight).isPresent()) {
                current = current.flatMap(Entry::getRight);
            } else {
                return Optional.empty();
            }
        }
    }
}
