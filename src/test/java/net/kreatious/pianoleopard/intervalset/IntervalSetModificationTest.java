package net.kreatious.pianoleopard.intervalset;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertThat;

import java.util.stream.IntStream;

import org.junit.Test;

/**
 * Tests for methods that modify {@link IntervalSet}
 *
 * @author Jay-R Studer
 */
public class IntervalSetModificationTest {
    private final IntervalSet<Integer, Integer> set = new IntervalSet<>();

    /**
     * Tests {@link IntervalSet#clear()}
     */
    @Test
    public void testClear() {
        addValues(10);

        set.clear();
        assertThat(set.size(), is(0));
        assertThat(set, is(emptyIterable()));
    }

    /**
     * Tests {@link IntervalSet#size()}
     */
    @Test
    public void testSize() {
        addValues(10);

        assertThat(set.size(), is(10));
        assertThat(set, iterableWithSize(10));
    }

    /**
     * Tests {@link IntervalSet#iterator()}
     */
    @Test
    public void testIterator() {
        final Integer[] addedValues = addValues(8);

        assertThat(set, contains(addedValues));
    }

    /**
     * Tests {@link IntervalSet#put(Comparable, Comparable, Object)}
     */
    @Test
    public void testPut() {
        for (int i = 0; i != 10; i++) {
            set.put(i, i, i);
            assertThat(set, contains(IntStream.rangeClosed(0, i).boxed().toArray()));
        }
    }

    private Integer[] addValues(int count) {
        final Integer[] addedValues = new Integer[count];
        for (int i = 0; i != count; i++) {
            set.put(i, i, i);
            addedValues[i] = i;
        }
        return addedValues;
    }
}
