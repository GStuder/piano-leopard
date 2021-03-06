package net.kreatious.pianoleopard.intervalset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

class Entry<V> {
    private Interval key;
    private long maximum;
    private List<V> values = new ArrayList<>();
    private Optional<Entry<V>> left = Optional.empty();
    private Optional<Entry<V>> right = Optional.empty();
    private Optional<Entry<V>> parent = Optional.empty();
    private boolean red;

    Entry(Interval key, V value, Optional<Entry<V>> parent) {
        Objects.requireNonNull(value);
        this.key = key;
        values.add(value);
        this.parent = parent;
        this.maximum = key.getHigh();
    }

    Optional<Entry<V>> getLeft() {
        return left;
    }

    Optional<Entry<V>> getRight() {
        return right;
    }

    Optional<Entry<V>> getParent() {
        return parent;
    }

    long getMaximum() {
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
    private Optional<Entry<V>> rotateLeft() {
        final Optional<Entry<V>> oldRight = right;
        Optional<Entry<V>> newRoot = Optional.empty();

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
    private Optional<Entry<V>> rotateRight() {
        final Optional<Entry<V>> oldLeft = left;
        Optional<Entry<V>> newRoot = Optional.empty();

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
    Optional<Entry<V>> rebalance(Optional<Entry<V>> root) {
        red = true;

        final AtomicReference<Optional<Entry<V>>> currentRoot = new AtomicReference<>(root);
        final Consumer<Entry<V>> assignToCurrentRoot = newRoot -> currentRoot.set(Optional.of(newRoot));
        final Consumer<Entry<V>> setRed = current -> current.red = true;
        final Consumer<Entry<V>> setBlack = current -> current.red = false;

        Optional<Entry<V>> node = Optional.of(this);
        while (node.isPresent() && !node.equals(currentRoot.get())
                && node.flatMap(Entry::getParent).filter(Entry::isRed).isPresent()) {
            final Optional<Entry<V>> nodeParent = node.flatMap(Entry::getParent);
            final Optional<Entry<V>> parentParent = nodeParent.flatMap(Entry::getParent);
            if (nodeParent.equals(parentParent.flatMap(Entry::getRight))) {
                final Optional<Entry<V>> parentParentLeft = parentParent.flatMap(Entry::getLeft);
                if (parentParentLeft.filter(Entry::isRed).isPresent()) {
                    nodeParent.ifPresent(setBlack);
                    parentParent.ifPresent(setRed);
                    parentParentLeft.ifPresent(setBlack);
                    node = parentParent;
                } else {
                    if (node.equals(nodeParent.flatMap(Entry::getLeft))) {
                        node = node.flatMap(Entry::getParent);
                        node.flatMap(Entry::rotateRight).ifPresent(assignToCurrentRoot);
                    }
                    final Optional<Entry<V>> newParent = node.flatMap(Entry::getParent);
                    final Optional<Entry<V>> newParentParent = newParent.flatMap(Entry::getParent);
                    newParent.ifPresent(setBlack);
                    newParentParent.ifPresent(setRed);
                    newParentParent.flatMap(Entry::rotateLeft).ifPresent(assignToCurrentRoot);
                }
            } else {
                final Optional<Entry<V>> parentParentRight = parentParent.flatMap(Entry::getRight);
                if (parentParentRight.filter(Entry::isRed).isPresent()) {
                    nodeParent.ifPresent(setBlack);
                    parentParent.ifPresent(setRed);
                    parentParentRight.ifPresent(setBlack);
                    node = parentParent;
                } else {
                    if (node.equals(nodeParent.flatMap(Entry::getRight))) {
                        node = node.flatMap(Entry::getParent);
                        node.flatMap(Entry::rotateLeft).ifPresent(assignToCurrentRoot);
                    }
                    final Optional<Entry<V>> newParent = node.flatMap(Entry::getParent);
                    final Optional<Entry<V>> newParentParent = newParent.flatMap(Entry::getParent);
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

    /**
     * Removes this node from the tree.
     *
     * @param root
     *            the current root node
     * @return the new root node
     */
    Optional<Entry<V>> remove(Optional<Entry<V>> root) {
        if (left.isPresent() && right.isPresent()) {
            Entry<V> inOrderSuccessor = right.get();
            while (inOrderSuccessor.left.isPresent()) {
                inOrderSuccessor = inOrderSuccessor.left.get();
            }

            key = inOrderSuccessor.key;
            values = inOrderSuccessor.values;
            return inOrderSuccessor.remove(root);
        }

        Optional<Entry<V>> currentRoot = root;
        if (left.isPresent() || right.isPresent()) {
            final Entry<V> onlyChild = left.isPresent() ? left.get() : right.get();

            onlyChild.parent = parent;
            if (parent.isPresent()) {
                parent.get().insertNode(onlyChild);
            } else {
                currentRoot = Optional.of(onlyChild);
            }

            left = Optional.empty();
            right = Optional.empty();
            parent = Optional.empty();

            if (!red) {
                currentRoot = onlyChild.deletionRebalance(currentRoot);
            }
        } else if (parent.isPresent()) {
            if (!red) {
                currentRoot = deletionRebalance(currentRoot);
            }

            final Entry<V> p = parent.get();
            if (p.left.equals(Optional.of(this))) {
                p.left = Optional.empty();
            } else if (p.right.equals(Optional.of(this))) {
                p.right = Optional.empty();
            }
            parent = Optional.empty();
        } else {
            currentRoot = Optional.empty();
        }

        return currentRoot;
    }

    /**
     * Rebalances the tree by performing tree rotations, used after deleting.
     *
     * @param root
     *            the current root node of the tree
     * @return the new root node of the tree
     */
    private Optional<Entry<V>> deletionRebalance(Optional<Entry<V>> root) {
        final AtomicReference<Optional<Entry<V>>> currentRoot = new AtomicReference<>(root);
        final Consumer<Entry<V>> assignToCurrentRoot = newRoot -> currentRoot.set(Optional.of(newRoot));
        final Consumer<Entry<V>> setRed = current -> current.red = true;
        final Consumer<Entry<V>> setBlack = current -> current.red = false;

        Optional<Entry<V>> node = Optional.of(this);
        while (!(node.equals(currentRoot.get()) || node.filter(Entry::isRed).isPresent())) {
            final Optional<Entry<V>> nodeParent = node.flatMap(Entry::getParent);
            final Optional<Entry<V>> nodeParentLeft = nodeParent.flatMap(Entry::getLeft);
            final Optional<Entry<V>> nodeParentRight = nodeParent.flatMap(Entry::getRight);
            if (node.equals(nodeParentRight)) {
                Optional<Entry<V>> sibling = nodeParentLeft;

                if (sibling.filter(Entry::isRed).isPresent()) {
                    sibling.ifPresent(setBlack);
                    nodeParent.ifPresent(setRed);
                    nodeParent.flatMap(Entry::rotateRight).ifPresent(assignToCurrentRoot);
                    sibling = nodeParentLeft;
                }

                final Optional<Entry<V>> siblingRight = sibling.flatMap(Entry::getRight);
                final Optional<Entry<V>> siblingLeft = sibling.flatMap(Entry::getLeft);
                if (siblingRight.filter(Entry::isRed).isPresent() || siblingLeft.filter(Entry::isRed).isPresent()) {
                    if (!siblingLeft.filter(Entry::isRed).isPresent()) {
                        siblingRight.ifPresent(setBlack);
                        sibling.ifPresent(setRed);
                        sibling.flatMap(Entry::rotateLeft).ifPresent(assignToCurrentRoot);
                        sibling = nodeParentLeft;
                    }
                    sibling.ifPresent(s -> s.red = nodeParent.filter(Entry::isRed).isPresent());
                    nodeParent.ifPresent(setRed);
                    siblingLeft.ifPresent(setBlack);
                    nodeParent.flatMap(Entry::rotateRight).ifPresent(assignToCurrentRoot);
                    node = currentRoot.get();
                } else {
                    sibling.ifPresent(setRed);
                    node = nodeParent;
                }
            } else {
                Optional<Entry<V>> sibling = nodeParentRight;
                if (sibling.filter(Entry::isRed).isPresent()) {
                    sibling.ifPresent(setBlack);
                    nodeParent.ifPresent(setRed);
                    nodeParent.flatMap(Entry::rotateLeft).ifPresent(assignToCurrentRoot);
                    sibling = nodeParentRight;
                }

                final Optional<Entry<V>> siblingLeft = sibling.flatMap(Entry::getLeft);
                final Optional<Entry<V>> siblingRight = sibling.flatMap(Entry::getRight);
                if (siblingLeft.filter(Entry::isRed).isPresent() || siblingRight.filter(Entry::isRed).isPresent()) {
                    if (!siblingRight.filter(Entry::isRed).isPresent()) {
                        siblingLeft.ifPresent(setBlack);
                        sibling.ifPresent(setRed);
                        sibling.flatMap(Entry::rotateRight).ifPresent(assignToCurrentRoot);
                        sibling = nodeParentRight;
                    }
                    sibling.ifPresent(s -> s.red = nodeParent.filter(Entry::isRed).isPresent());
                    nodeParent.ifPresent(setBlack);
                    siblingRight.ifPresent(setBlack);
                    nodeParent.flatMap(Entry::rotateLeft).ifPresent(assignToCurrentRoot);
                    node = currentRoot.get();
                } else {
                    sibling.ifPresent(setRed);
                    node = nodeParent;
                }
            }
        }
        augment();

        node.ifPresent(setBlack);
        return currentRoot.get();
    }

    private void augment() {
        Optional<Entry<V>> node = Optional.of(this);
        do {
            node.get().maximum = Stream
                    .concat(Stream.<Optional<Entry<V>>> of(node.flatMap(Entry::getLeft), node.flatMap(Entry::getRight))
                            .filter(Optional::isPresent).map(child -> child.get().maximum),
                            Stream.of(node.get().getKey().getHigh())).max(Long::compare).get();
            node = node.flatMap(Entry::getParent);
        } while (node.isPresent());
    }

    /**
     * Finds the entry with the specified key.
     *
     * @return the entry with the specified key, otherwise empty.
     */
    Optional<Entry<V>> binarySearchExact(Interval searchKey) {
        Optional<Entry<V>> current = Optional.of(this);
        do {
            final int comparison = searchKey.compareTo(current.get().getKey());
            if (comparison < 0) {
                current = current.flatMap(Entry::getLeft);
            } else if (comparison > 0) {
                current = current.flatMap(Entry::getRight);
            } else {
                return current;
            }
        } while (current.isPresent());
        return Optional.empty();
    }

    /**
     * Finds the insertion position of a new entry with the specified key.
     * <p>
     * If the search key already exists, the corresponding entry is returned,
     * otherwise the entry which should be the parent of the inserted value is
     * returned.
     *
     * @return the insertion position for the specified interval, never empty.
     */
    Optional<Entry<V>> binarySearchInexact(Interval searchKey) {
        Optional<Entry<V>> current = Optional.of(this);
        while (true) {
            final Optional<Entry<V>> previous = current;
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
     * Inserts a entry as a child of this entry, without rebalancing the tree.
     */
    void insertNode(Entry<V> entry) {
        if (entry.key.compareTo(getKey()) < 0) {
            left = Optional.of(entry);
        } else {
            right = Optional.of(entry);
        }
    }

    Interval getKey() {
        return key;
    }

    Collection<V> getValues() {
        return values;
    }

    /**
     * Adds a value to this entry.
     * <p>
     * Entries do not allow duplicates and act as a set.
     *
     * @param value
     *            the value to add
     * @return true if a value was added, false otherwise
     */
    boolean addValue(V value) {
        Objects.requireNonNull(value);
        if (values.contains(value)) {
            return false;
        }
        values.add(value);
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Entry)) {
            return false;
        } else {
            final Entry<?> other = (Entry<?>) o;
            return key == other.getKey();
        }
    }

    @Override
    public int hashCode() {
        return key.hashCode() ^ values.hashCode();
    }

    @Override
    public String toString() {
        return key + "=" + maximum;
    }
}
