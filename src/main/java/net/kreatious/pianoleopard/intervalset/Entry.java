package net.kreatious.pianoleopard.intervalset;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

class Entry<K extends Comparable<K>, V> implements Map.Entry<Interval<K>, V> {
    private final Interval<K> key;
    private K maximum;
    private V value;
    private Optional<Entry<K, V>> left = Optional.empty();
    private Optional<Entry<K, V>> right = Optional.empty();
    private Optional<Entry<K, V>> parent = Optional.empty();
    private boolean red;

    Entry(Interval<K> key, V value, Optional<Entry<K, V>> parent) {
        this.key = key;
        this.value = value;
        this.parent = parent;
        this.maximum = key.getHigh();
    }

    Optional<Entry<K, V>> getLeft() {
        return left;
    }

    Optional<Entry<K, V>> getRight() {
        return right;
    }

    Optional<Entry<K, V>> getParent() {
        return parent;
    }

    K getMaximum() {
        return maximum;
    }

    private boolean isRed() {
        return red;
    }

    /**
     * Performs a left tree rotation about this entry
     *
     * @return the new root node to set, if any, otherwise an empty optional
     */
    private Optional<Entry<K, V>> rotateLeft() {
        final Optional<Entry<K, V>> oldRight = right;
        Optional<Entry<K, V>> newRoot = Optional.empty();

        right = oldRight.flatMap(Entry::getLeft);
        oldRight.flatMap(Entry::getLeft).ifPresent(rightLeft -> rightLeft.parent = Optional.of(this));
        oldRight.get().parent = parent;

        if (parent.flatMap(Entry::getLeft).equals(Optional.of(this))) {
            parent.get().left = oldRight;
        } else if (parent.isPresent()) {
            parent.get().right = oldRight;
        } else {
            newRoot = oldRight;
        }

        oldRight.get().left = Optional.of(this);
        parent = oldRight;
        augment();
        return newRoot;
    }

    /**
     * Performs a right tree rotation about this entry
     *
     * @return the new root node to set, if any, otherwise an empty optional
     */
    private Optional<Entry<K, V>> rotateRight() {
        final Optional<Entry<K, V>> oldLeft = left;
        Optional<Entry<K, V>> newRoot = Optional.empty();

        left = oldLeft.flatMap(Entry::getRight);
        oldLeft.flatMap(Entry::getRight).ifPresent(leftRight -> leftRight.parent = Optional.of(this));
        oldLeft.get().parent = parent;

        if (parent.flatMap(Entry::getRight).equals(Optional.of(this))) {
            parent.get().right = oldLeft;
        } else if (parent.isPresent()) {
            parent.get().left = oldLeft;
        } else {
            newRoot = oldLeft;
        }

        oldLeft.get().right = Optional.of(this);
        parent = oldLeft;
        augment();
        return newRoot;
    }

    /**
     * Rebalances the tree by performing tree rotations
     *
     * @param root
     *            the current root node of the tree
     * @return the new root node of the tree
     */
    Optional<Entry<K, V>> rebalance(Optional<Entry<K, V>> root) {
        red = true;

        final AtomicReference<Optional<Entry<K, V>>> currentRoot = new AtomicReference<>(root);
        final Consumer<Entry<K, V>> assignToCurrentRoot = newRoot -> currentRoot.set(Optional.of(newRoot));
        final Consumer<Entry<K, V>> setRed = current -> current.red = true;
        final Consumer<Entry<K, V>> setBlack = current -> current.red = false;

        Optional<Entry<K, V>> node = Optional.of(this);
        while (node.isPresent() && !node.equals(currentRoot.get())
                && node.flatMap(Entry::getParent).map(Entry::isRed).orElse(false)) {
            final Optional<Entry<K, V>> nodeParent = node.flatMap(Entry::getParent);
            final Optional<Entry<K, V>> parentParent = nodeParent.flatMap(Entry::getParent);
            if (nodeParent.equals(parentParent.flatMap(Entry::getRight))) {
                final Optional<Entry<K, V>> parentParentLeft = parentParent.flatMap(Entry::getLeft);
                if (parentParentLeft.map(Entry::isRed).orElse(false)) {
                    nodeParent.ifPresent(setBlack);
                    parentParent.ifPresent(setRed);
                    parentParentLeft.ifPresent(setBlack);
                    node = parentParent;
                } else {
                    if (node.equals(nodeParent.flatMap(Entry::getLeft))) {
                        node = node.flatMap(Entry::getParent);
                        node.flatMap(Entry::rotateRight).ifPresent(assignToCurrentRoot);
                    }
                    final Optional<Entry<K, V>> newParent = node.flatMap(Entry::getParent);
                    final Optional<Entry<K, V>> newParentParent = newParent.flatMap(Entry::getParent);
                    newParent.ifPresent(setBlack);
                    newParentParent.ifPresent(setRed);
                    newParentParent.flatMap(Entry::rotateLeft).ifPresent(assignToCurrentRoot);
                }
            } else {
                final Optional<Entry<K, V>> parentParentRight = parentParent.flatMap(Entry::getRight);
                if (parentParentRight.map(Entry::isRed).orElse(false)) {
                    nodeParent.ifPresent(setBlack);
                    parentParent.ifPresent(setRed);
                    parentParentRight.ifPresent(setBlack);
                    node = parentParent;
                } else {
                    if (node.equals(nodeParent.flatMap(Entry::getRight))) {
                        node = node.flatMap(Entry::getParent);
                        node.flatMap(Entry::rotateLeft).ifPresent(assignToCurrentRoot);
                    }
                    final Optional<Entry<K, V>> newParent = node.flatMap(Entry::getParent);
                    final Optional<Entry<K, V>> newParentParent = newParent.flatMap(Entry::getParent);
                    newParent.ifPresent(setBlack);
                    newParentParent.ifPresent(setRed);
                    newParentParent.flatMap(Entry::rotateRight).ifPresent(assignToCurrentRoot);
                }
            }
        }
        augment();

        currentRoot.get().ifPresent(setBlack);
        return currentRoot.get();
    }

    private void augment() {
        Optional<Entry<K, V>> node = Optional.of(this);
        do {
            node.get().maximum = Stream
                    .concat(Stream
                            .<Optional<Entry<K, V>>> of(node.flatMap(Entry::getLeft), node.flatMap(Entry::getRight))
                            .filter(Optional::isPresent).map(child -> child.get().maximum),
                            Stream.of(node.get().getKey().getHigh())).max(Comparator.naturalOrder()).get();
            node = node.flatMap(Entry::getParent);
        } while (node.isPresent());
    }

    /**
     * Finds the insertion position of a new entry with the specified key.
     */
    Optional<Entry<K, V>> binarySearch(Interval<K> searchKey) {
        Optional<Entry<K, V>> current = Optional.of(this);
        while (true) {
            final Optional<Entry<K, V>> previous = current;
            final int comparison = searchKey.compareTo(current.get().getKey());
            if (comparison < 0) {
                current = current.flatMap(Entry::getLeft);
            } else if (comparison > 0) {
                current = current.flatMap(Entry::getRight);
            } else {
                return current;
            }

            if (!current.isPresent()) {
                return previous;
            }
        }
    }

    /**
     * Inserts a entry as a child of this entry
     */
    void insertNode(Entry<K, V> entry) {
        if (entry.key.compareTo(getKey()) < 0) {
            left = Optional.of(entry);
        } else {
            right = Optional.of(entry);
        }
    }

    @Override
    public Interval<K> getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        final V result = this.value;
        this.value = value;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Map.Entry)) {
            return false;
        } else {
            final Map.Entry<?, ?> other = (Map.Entry<?, ?>) o;
            return key.equals(other.getKey());
        }
    }

    @Override
    public int hashCode() {
        return key.hashCode() ^ value.hashCode();
    }

    @Override
    public String toString() {
        return key + "=" + maximum;
    }
}
