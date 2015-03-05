package net.kreatious.pianoleopard;

import java.util.Arrays;

/**
 * Data structure holding a set of raw MIDI keys (notes).
 * <p>
 * The API is the same as a standard Java collection, just specialized to
 * minimize object allocation.
 *
 * @author Jay-R Studer
 */
class Keys {
    private final boolean[] keyStates = new boolean[128];

    private final int[] addedKeys = new int[128];
    private final KeyIterator iterator = new KeyIterator();
    private int size;

    void add(int key) {
        if (contains(key)) {
            return;
        }
        keyStates[key] = true;
        addedKeys[size] = key;
        size++;
    }

    boolean contains(int key) {
        return keyStates[key];
    }

    KeyIterator iterator() {
        iterator.reset();
        return iterator;
    }

    void clear() {
        Arrays.fill(keyStates, false);
        size = 0;
    }

    @Override
    public String toString() {
        return Arrays.toString(Arrays.copyOf(addedKeys, size));
    }

    class KeyIterator {
        private static final int REMOVED = -1;
        private int index;

        private void reset() {
            index = 0;
        }

        boolean hasNext() {
            if (index != size) {
                return true;
            }

            // Moves the removed values to the end of the array
            int result = 0;
            for (int first = 0; first != size; first++) {
                if (addedKeys[first] != REMOVED) {
                    addedKeys[result] = addedKeys[first];
                    result++;
                }
            }
            size = result;
            return false;
        }

        int next() {
            return addedKeys[index++];
        }

        void remove() {
            keyStates[addedKeys[index - 1]] = false;
            addedKeys[index - 1] = REMOVED;
        }
    }
}
