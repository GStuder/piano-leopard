package net.kreatious.pianoleopard.intervalset;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Provides fast ordered iteration of values within a range
 *
 * @author Jay-R Studer
 */
class RangeIterator<K extends Comparable<K>, V> implements Iterator<V> {
    private final IntervalSet<K, V> set;
    private final int expectedModifications;
    private final Interval<K> range;
    private Optional<Entry<K, V>> next;

    RangeIterator(IntervalSet<K, V> set, Interval<K> range, Optional<Entry<K, V>> first) {
        this.set = set;
        this.range = range;
        expectedModifications = set.getModifications();
        next = first;
    }

    @Override
    public final boolean hasNext() {
        return next.isPresent();
    }

    @Override
    public V next() {
        if (!next.isPresent()) {
            throw new NoSuchElementException();
        } else if (set.getModifications() != expectedModifications) {
            throw new ConcurrentModificationException();
        }

        final Optional<Entry<K, V>> result = next;
        next = successor(result);
        return result.get().getValue();
    }

    private Optional<Entry<K, V>> successor(Optional<Entry<K, V>> entry) {
        final Predicate<Entry<K, V>> withinResult = current -> current.getKey().containsInterval(range);
        final Predicate<Entry<K, V>> maximumIsAfterRangeStart = current -> current.getMaximum().compareTo(
                range.getLow()) >= 0;
        final Predicate<Entry<K, V>> lowIsBeforeRangeEnd = current -> current.getKey().getLow()
                .compareTo(range.getHigh()) <= 0;

        if (entry.filter(lowIsBeforeRangeEnd).flatMap(Entry::getRight).filter(maximumIsAfterRangeStart).isPresent()) {
            Optional<Entry<K, V>> current = entry.flatMap(Entry::getRight);
            while (current.flatMap(Entry::getLeft).filter(maximumIsAfterRangeStart).isPresent()) {
                current = current.flatMap(Entry::getLeft);
            }
            return current.filter(withinResult);
        }

        Optional<Entry<K, V>> parent = entry.flatMap(Entry::getParent);
        Optional<Entry<K, V>> child = entry;
        while (parent.flatMap(Entry::getRight).filter(maximumIsAfterRangeStart).equals(child)) {
            child = parent;
            parent = parent.flatMap(Entry::getParent);
        }
        return parent.filter(withinResult);
    }
}
