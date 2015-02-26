package net.kreatious.pianoleopard.intervalset;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;

class RangeIterable<K extends Comparable<K>, V> implements Iterable<V> {
    private final IntervalSet<K, V> set;
    private final Interval<K> range;
    private final Optional<Entry<K, V>> first;

    RangeIterable(IntervalSet<K, V> intervalSet, K lowKey, K highKey) {
        set = intervalSet;
        range = new Interval<>(lowKey, highKey);
        first = first();
    }

    @Override
    public Iterator<V> iterator() {
        return new RangeIterator<>(set, range, first);
    }

    private Optional<Entry<K, V>> first() {
        final Predicate<Entry<K, V>> withinResult = current -> current.getKey().containsInterval(range);
        final Predicate<Entry<K, V>> maximumIsAfterRangeStart = current -> current.getMaximum().compareTo(
                range.getLow()) >= 0;

        Optional<Entry<K, V>> current = set.getRoot();
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
