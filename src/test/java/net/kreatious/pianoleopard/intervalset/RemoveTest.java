package net.kreatious.pianoleopard.intervalset;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

/**
 * Tests for {@link IntervalSet}'s remove operation
 *
 * @author Jay-R Studer
 */
@RunWith(Parameterized.class)
public class RemoveTest {
    private static final Set<Interval<Integer>> INTERVALS = new HashSet<>();
    static {
        final Random rnd = new Random(311);
        for (int i = 0; i != 200; i++) {
            final int low = rnd.nextInt(25);
            final int high = low + rnd.nextInt(25);
            INTERVALS.add(new Interval<>(low, high));
        }
    }

    private final IntervalSet<Integer, String> set = new IntervalSet<>();

    /**
     * The bounds for the test case
     */
    @Parameter(0)
    public Interval<Integer> interval;

    /**
     * The values expected to be returned by the test
     */
    @Parameter(1)
    public List<String> expectedValues;

    /**
     * Provides a list of parameters to inject into the parameter fields
     *
     * @return a list of test cases
     */
    @Parameters(name = "removing interval [{0}]")
    public static List<Object[]> parameters() {
        final List<Object[]> tests = new ArrayList<>();
        for (final Interval<Integer> testInterval : INTERVALS) {
            final List<String> expectedValues = INTERVALS.stream().filter(interval -> !interval.equals(testInterval))
                    .map(Interval::toString).collect(toList());
            tests.add(new Object[] { testInterval, expectedValues });
        }
        return tests;
    }

    /**
     * Initializes the test fixture with a constant set of intervals
     */
    @Before
    public void constructSet() {
        INTERVALS.forEach(x -> set.put(x.getLow(), x.getHigh(), x.toString()));
    }

    /**
     * Tests that {@link IntervalSet#removeFirst} removes only one element
     */
    @Test
    public void testRemove() {
        set.removeFirst(interval.getLow(), interval.getHigh(), (x) -> true);

        final Iterable<String> result = Lists.newArrayList(set.iterator());
        expectedValues.forEach(value -> assertThat(result, hasItem(value)));
        assertThat(result, iterableWithSize(expectedValues.size()));
    }
}
