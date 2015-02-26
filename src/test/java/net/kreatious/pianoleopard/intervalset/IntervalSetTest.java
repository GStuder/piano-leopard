package net.kreatious.pianoleopard.intervalset;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for {@link IntervalSet}
 *
 * @author Jay-R Studer
 */
@RunWith(Parameterized.class)
public class IntervalSetTest {
    private static final IntervalSet<Integer, String> SET = new IntervalSet<>();

    /**
     * Bounds for the test case
     */
    @Parameter(0)
    public Interval<Integer> interval;

    /**
     * The values expected to be returned by the test.
     * <p>
     * These are calculated using the naive O(N) algorithm.
     */
    @Parameter(1)
    public List<String> expectedValues;

    /**
     * Provides a list of parameters to inject into the parameter fields
     *
     * @return a list of test cases
     */
    @Parameters(name = "interval [{0}] contains {1}")
    public static List<Object[]> parameters() {
        final List<Interval<Integer>> intervals = createTestIntervals();
        intervals.forEach(interval -> SET.put(interval.getLow(), interval.getHigh(), interval.toString()));

        final List<Object[]> tests = new ArrayList<>();
        final int max = intervals.stream().map(Interval::getHigh).max(Integer::compare).get();
        for (int i = 0; i <= max; i++) {
            for (int intervalSize = 0; intervalSize != 10; intervalSize++) {
                final Interval<Integer> testInterval = new Interval<>(i, i + intervalSize);

                final List<String> expectedValues = intervals.stream()
                        .filter(interval -> interval.containsInterval(testInterval)).map(Interval::toString)
                        .collect(toList());
                tests.add(new Object[] { testInterval, expectedValues });
            }
        }
        return tests;
    }

    /**
     * Tests that {@link IntervalSet#subSet} returns only the expected values
     */
    @Test
    public void testSubSet() {
        final Iterable<String> result = SET.subSet(interval.getLow(), interval.getHigh());
        expectedValues.forEach(value -> assertThat(result, hasItem(value)));
        assertThat(result, iterableWithSize(expectedValues.size()));
    }

    private static List<Interval<Integer>> createTestIntervals() {
        final List<Interval<Integer>> intervals = new ArrayList<>();
        for (int i = 0; i != 5; i++) {
            // Points
            intervals.add(new Interval<>(i, i));

            // Non overlapping intervals - [10, 11], [12, 13]
            intervals.add(new Interval<>(10 + i * 2, 10 + i * 2 + 1));

            // Touching intervals - [20, 22], [22, 24]
            intervals.add(new Interval<>(20 + i * 2, 20 + i * 2 + 2));

            // Overlapping intervals by 1 - [40, 43], [42, 45]
            intervals.add(new Interval<>(40 + i * 2, 40 + i * 2 + 3));

            // Overlapping intervals by 3 - [60, 64], [62, 66]
            intervals.add(new Interval<>(60 + i * 2, 60 + i * 2 + 4));
        }
        return intervals;
    }
}
