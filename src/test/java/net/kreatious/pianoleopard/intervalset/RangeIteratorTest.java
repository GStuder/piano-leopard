package net.kreatious.pianoleopard.intervalset;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;

/**
 * Tests for {@link IntervalSet}'s range iterator
 *
 * @author Jay-R Studer
 */
@RunWith(Parameterized.class)
public class RangeIteratorTest {
    private static final IntervalSet<Integer, String> SET = new IntervalSet<>();

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
    @Parameters(name = "interval [{0}] contains {1}")
    public static List<Object[]> parameters() {
        final List<Interval<Integer>> intervals = new ArrayList<>();
        final Random rnd = new Random(311);
        for (int i = 0; i != 63; i++) {
            final int low = rnd.nextInt(70);
            final int high = low + rnd.nextInt(50);
            intervals.add(new Interval<>(low, high));
        }
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
        final Iterable<String> result = Lists.newArrayList(SET.subSet(interval.getLow(), interval.getHigh()));
        expectedValues.forEach(value -> assertThat(result, hasItem(value)));
        assertThat(result, iterableWithSize(expectedValues.size()));
    }
}
