package net.kreatious.pianoleopard.intervalset;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * An ordered tree data structure for holding intervals. The user can
 * efficiently find all intervals that overlap with a given interval or point.
 * <p>
 * This set does not support null keys or values, but does support duplicate
 * intervals.
 *
 * @author Jay-R Studer
 * @param <K>
 *            the type of keys maintained by this set
 * @param <V>
 *            the type of mapped values
 */
public class IntervalSet<K extends Comparable<K>, V> implements Iterable<V> {
    private Optional<Entry<K, V>> root = Optional.empty();
    private int size;
    private int modifications;

    /**
     * Removes all values from this set.
     */
    public void clear() {
        root = Optional.empty();
        size = 0;
        modifications++;
    }

    /**
     * Returns the size of this set.
     *
     * @return the number of values in this collection
     */
    public int size() {
        return size;
    }

    /**
     * Associates the specified interval with the specified value in this set.
     * If this set previously contained a mapping for the interval, the old
     * value is replaced. If the specified interval is invalid, nothing is
     * inserted.
     *
     * @param low
     *            the low end of the range the specified value to associate
     * @param high
     *            the high end of the range the specified value to associate
     * @param value
     *            value to be associated with the specified key
     * @return the previous value associated with the interval, or empty if
     *         there was no mapping for the interval.
     * @throws NullPointerException
     *             if the specified key or value is null
     */
    public Optional<V> put(K low, K high, V value) {
        if (low.compareTo(high) > 0) {
            return Optional.empty();
        }

        final Interval<K> key = new Interval<>(low, high);
        if (!root.isPresent()) {
            root = Optional.of(new Entry<>(key, value, Optional.empty()));
            size = 1;
            modifications++;
            return Optional.empty();
        }

        final Optional<Entry<K, V>> parent = root.get().binarySearchInexact(key);
        if (parent.filter(p -> p.getKey().compareTo(key) == 0).isPresent()) {
            if (parent.get().addValue(value)) {
                size++;
                modifications++;
                return Optional.empty();
            }
            return Optional.of(value);
        }

        final Entry<K, V> entry = new Entry<>(key, value, parent);
        parent.get().insertNode(entry);
        root = entry.rebalance(root);
        size++;
        modifications++;
        return Optional.empty();
    }

    /**
     * Removes the first value for the specified interval from this set that
     * matches the given criteria.
     *
     * @param low
     *            the low end of the range the specified value to associate
     * @param high
     *            the high end of the range the specified value to associate
     * @param criteria
     *            the value to delete
     * @return the removed value, or empty if no value was removed
     * @throws IllegalArgumentException
     *             if {@code low} is greater than {@code high}
     * @throws NullPointerException
     *             if the specified key is null
     */
    public Optional<V> removeFirst(K low, K high, Predicate<? super V> criteria) {
        final Optional<Entry<K, V>> entryToRemove = root.flatMap(entry -> entry.binarySearchExact(new Interval<>(low,
                high)));
        if (!entryToRemove.isPresent()) {
            return Optional.empty();
        }

        final Entry<K, V> entry = entryToRemove.get();
        final Optional<V> result = entry.getValues().stream().filter(criteria).findAny();
        if (!result.isPresent()) {
            return Optional.empty();
        }

        entry.getValues().remove(result.get());
        if (entry.getValues().isEmpty()) {
            root = entry.remove(root);
        }
        size--;
        modifications++;
        return result;
    }

    /**
     * Returns an iterable read only view of the values in this set that
     * overlaps the specified interval. If {@code low} and {@code high} are
     * equal, the returned view contains the intervals containing a single
     * point.
     *
     * @param low
     *            low portion of the interval to retrieve
     * @param high
     *            high portion of the interval to retrieve
     * @throws IllegalArgumentException
     *             if {@code low} is greater than {@code high}
     * @return a read only view of the portion of this set overlapping the
     *         specified interval.
     */
    public Iterable<V> subSet(K low, K high) {
        return new RangeIterable<>(this, low, high);
    }

    @Override
    public Iterator<V> iterator() {
        return new InOrderIterator<>(this);
    }

    int getModifications() {
        return modifications;
    }

    Optional<Entry<K, V>> getRoot() {
        return root;
    }
}
