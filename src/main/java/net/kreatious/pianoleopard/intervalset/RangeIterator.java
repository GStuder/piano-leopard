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

    // Cached to avoid unnecessary object allocation in next()
    private final Predicate<Entry<K, V>> withinResult;
    private final Predicate<Entry<K, V>> maximumIsAfterRangeStart;
    private final Predicate<Entry<K, V>> lowIsBeforeRangeEnd;
    private final Predicate<Entry<K, V>> lowBeforeEndWithinResult;

    private Optional<Entry<K, V>> next;

    // Nullable - performance reasons
    private Iterator<V> subiterator;

    RangeIterator(IntervalSet<K, V> set, Interval<K> range, Optional<Entry<K, V>> first) {
        this.set = set;
        withinResult = c -> c.getKey().containsInterval(range);
        maximumIsAfterRangeStart = c -> c.getMaximum().compareTo(range.getLow()) >= 0;
        lowIsBeforeRangeEnd = c -> c.getKey().getLow().compareTo(range.getHigh()) <= 0;
        lowBeforeEndWithinResult = lowIsBeforeRangeEnd.and(withinResult.negate());

        expectedModifications = set.getModifications();
        next = first;
        if (first.isPresent()) {
            subiterator = first.get().getValues().iterator();
        }
    }

    @Override
    public final boolean hasNext() {
        return subiterator != null && subiterator.hasNext();
    }

    @Override
    public V next() {
        if (subiterator == null) {
            throw new NoSuchElementException();
        } else if (set.getModifications() != expectedModifications) {
            throw new ConcurrentModificationException();
        }

        final V result = subiterator.next();
        if (!subiterator.hasNext()) {
            next = successor(next);
            subiterator = next.isPresent() ? next.get().getValues().iterator() : null;
        }
        return result;
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
