package net.kreatious.pianoleopard.intervalset;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Provides in order iteration over an entire set
 *
 * @author Jay-R Studer
 */
class InOrderIterator<K extends Comparable<K>, V> implements Iterator<V> {
    private final IntervalSet<K, V> set;
    private final int expectedModifications;
    private Optional<Entry<K, V>> next;

    InOrderIterator(IntervalSet<K, V> set) {
        this.set = set;
        expectedModifications = set.getModifications();
        next = findSmallestEntry();
    }

    private Optional<Entry<K, V>> findSmallestEntry() {
        Optional<Entry<K, V>> leftMost = set.getRoot();
        while (leftMost.flatMap(Entry::getLeft).isPresent()) {
            leftMost = leftMost.flatMap(Entry::getLeft);
        }
        return leftMost;
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
        if (entry.flatMap(Entry::getRight).isPresent()) {
            Optional<Entry<K, V>> current = entry.flatMap(Entry::getRight);
            while (current.flatMap(Entry::getLeft).isPresent()) {
                current = current.flatMap(Entry::getLeft);
            }
            return current;
        }

        Optional<Entry<K, V>> parent = entry.flatMap(Entry::getParent);
        Optional<Entry<K, V>> child = entry;
        while (parent.flatMap(Entry::getRight).equals(child)) {
            child = parent;
            parent = parent.flatMap(Entry::getParent);
        }
        return parent;
    }
}
