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

    // Cached to avoid unnecessary object allocation in next()
    private final Predicate<Entry<K, V>> withinResult = c -> c.getKey().containsInterval(range);
    private final Predicate<Entry<K, V>> maximumIsAfterRangeStart = c -> c.getMaximum().compareTo(range.getLow()) >= 0;
    private final Predicate<Entry<K, V>> lowIsBeforeRangeEnd = c -> c.getKey().getLow().compareTo(range.getHigh()) <= 0;
    private final Predicate<Entry<K, V>> lowBeforeEndWithinResult = lowIsBeforeRangeEnd.and(withinResult.negate());

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
        // Visit next child
        if (entry.filter(lowIsBeforeRangeEnd).flatMap(Entry::getRight).filter(maximumIsAfterRangeStart).isPresent()) {
            Optional<Entry<K, V>> current = entry.flatMap(Entry::getRight);
            do {
                if (current.flatMap(Entry::getLeft).filter(maximumIsAfterRangeStart).isPresent()) {
                    current = current.flatMap(Entry::getLeft);
                } else if (current.filter(lowBeforeEndWithinResult).isPresent()) {
                    current = current.flatMap(Entry::getRight);
                } else {
                    return current.filter(withinResult);
                }
            } while (true);
        }

        // Find parent node whose right child is the current node
        Optional<Entry<K, V>> parent = entry.flatMap(Entry::getParent);
        Optional<Entry<K, V>> child = entry;
        while (parent.flatMap(Entry::getRight).equals(child)) {
            child = parent;
            parent = parent.flatMap(Entry::getParent);
        }

        // Parent node contains the requested range
        if (parent.filter(withinResult).isPresent()) {
            return parent;
        }

        // Parent's right child contains additional intervals
        if (parent.isPresent()) {
            return successor(parent);
        }

        return Optional.empty();
    }
}
