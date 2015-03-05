package net.kreatious.pianoleopard;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import net.kreatious.pianoleopard.Keys.KeyIterator;

import org.junit.Test;

/**
 * Tests for {@link Keys} data structure
 *
 * @author Jay-R Studer
 */
public class KeysTest {
    private final Keys keys = new Keys();
    private final Random rnd = new Random(1);

    /**
     * Tests for {@link Keys#add}, {@link Keys#contains}, and its iterator
     */
    @Test
    public void testAdd() {
        final Set<Integer> addedKeys = new HashSet<>();
        for (int i = 0; i != 10; i++) {
            final int key = rnd.nextInt(128);
            keys.add(key);
            addedKeys.add(key);

            assertThat(keys.contains(key), is(true));
            assertThat(values(keys), containsInAnyOrder(addedKeys.toArray(new Integer[0])));
        }
    }

    /**
     * Tests for removal using the iterator
     */
    @Test
    public void testRemove() {
        // Given a set containing several keys
        final Set<Integer> addedKeys = new HashSet<>();
        for (int i = 0; i != 10; i++) {
            final int key = rnd.nextInt(128);
            keys.add(key);
            addedKeys.add(key);
        }

        // When some keys are removed
        final KeyIterator it = keys.iterator();
        while (it.hasNext()) {
            final int key = it.next();
            if (rnd.nextBoolean()) {
                it.remove();
                addedKeys.remove(key);
            }
        }

        // The behavior matches a standard set
        assertThat(values(keys), containsInAnyOrder(addedKeys.toArray(new Integer[0])));
    }

    /**
     * Tests for removal of all values using the iterator
     */
    @Test
    public void testRemoveAll() {
        IntStream.rangeClosed(0, 127).forEach(keys::add);

        final KeyIterator iterator = keys.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }

        assertThat(values(keys), is(empty()));
    }

    /**
     * Tests for {@link Keys#clear}
     */
    @Test
    public void testClear() {
        IntStream.rangeClosed(0, 127).forEach(keys::add);

        keys.clear();

        assertThat(values(keys), is(empty()));
        IntStream.rangeClosed(0, 127).filter(keys::contains)
                .forEach(key -> fail("expected " + key + " to not be contained"));
    }

    /**
     * Tests for removal of the first value using the iterator
     */
    @Test
    public void testRemoveFirst() {
        IntStream.rangeClosed(0, 127).forEach(keys::add);

        final KeyIterator iterator = keys.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == 0) {
                iterator.remove();
            }
        }

        assertThat(values(keys), is(IntStream.rangeClosed(1, 127).boxed().collect(toList())));
    }

    /**
     * Tests for removal of the last value using the iterator
     */
    @Test
    public void testRemoveLast() {
        IntStream.rangeClosed(0, 127).forEach(keys::add);

        final KeyIterator iterator = keys.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == 127) {
                iterator.remove();
            }
        }

        assertThat(values(keys), is(IntStream.rangeClosed(0, 126).boxed().collect(toList())));
    }

    /**
     * Tests for {@link Keys#contains}
     */
    @Test
    public void testContains() {
        keys.add(0);
        assertThat(keys.contains(0), is(true));

        final KeyIterator iterator = keys.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        assertThat(values(keys), is(empty()));
        assertThat(keys.contains(0), is(false));
    }

    private static List<Integer> values(Keys keys) {
        final List<Integer> result = new ArrayList<>();
        final KeyIterator iterator = keys.iterator();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }
}
