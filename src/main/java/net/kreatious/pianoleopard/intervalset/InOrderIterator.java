package net.kreatious.pianoleopard.intervalset;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Provides in order iteration over an entire set
 *
 * @author Jay-R Studer
 */
class InOrderIterator<V> implements Iterator<V> {
    private final IntervalSet<V> set;
    private final int expectedModifications;
    private Optional<Entry<V>> next;
    private Optional<Iterator<V>> subiterator;

    InOrderIterator(IntervalSet<V> set) {
        this.set = set;
        expectedModifications = set.getModifications();
        next = findSmallestEntry();
        subiterator = next.map(Entry::getValues).map(Collection::iterator);
    }

    private Optional<Entry<V>> findSmallestEntry() {
        Optional<Entry<V>> leftMost = set.getRoot();
        while (leftMost.flatMap(Entry::getLeft).isPresent()) {
            leftMost = leftMost.flatMap(Entry::getLeft);
        }
        return leftMost;
    }

    @Override
    public final boolean hasNext() {
        return subiterator.map(Iterator::hasNext).orElse(false);
    }

    @Override
    public V next() {
        if (!subiterator.isPresent()) {
            throw new NoSuchElementException();
        } else if (set.getModifications() != expectedModifications) {
            throw new ConcurrentModificationException();
        }

        final V result = subiterator.map(Iterator::next).get();
        if (!subiterator.map(Iterator::hasNext).orElse(false)) {
            next = successor(next);
            subiterator = next.map(Entry::getValues).map(Collection::iterator);
        }
        return result;
    }

    private Optional<Entry<V>> successor(Optional<Entry<V>> entry) {
        if (entry.flatMap(Entry::getRight).isPresent()) {
            Optional<Entry<V>> current = entry.flatMap(Entry::getRight);
            while (current.flatMap(Entry::getLeft).isPresent()) {
                current = current.flatMap(Entry::getLeft);
            }
            return current;
        }

        Optional<Entry<V>> parent = entry.flatMap(Entry::getParent);
        Optional<Entry<V>> child = entry;
        while (parent.flatMap(Entry::getRight).equals(child)) {
            child = parent;
            parent = parent.flatMap(Entry::getParent);
        }
        return parent;
    }
}
