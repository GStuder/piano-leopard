package net.kreatious.pianoleopard;

import static net.kreatious.pianoleopard.TempoController.factorToLinear;
import static net.kreatious.pianoleopard.TempoController.linearToFactor;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Tests for {@link TempoController}
 *
 * @author Jay-R Studer
 */
public class TempoControllerTest {
    private static final int LOWEST_SLIDER_VALUE = 0;
    private static final int DEFAULT_SLIDER_VALUE = TempoController.MIDPOINT;
    private static final int HIGHEST_SLIDER_VALUE = TempoController.MIDPOINT * 2;

    private static final double LOWEST_TEMPO_FACTOR = 1.0 / TempoController.HIGHEST_TEMPO_FACTOR;
    private static final double DEFAULT_TEMPO_FACTOR = 1.0;
    private static final double HIGHEST_TEMPO_FACTOR = TempoController.HIGHEST_TEMPO_FACTOR;

    /**
     * Tests that the functions are inverse functions
     */
    @Test
    public void testAll() {
        for (int i = LOWEST_SLIDER_VALUE; i != HIGHEST_SLIDER_VALUE; i++) {
            assertThat((double) factorToLinear(linearToFactor(i)), is(closeTo(i, 1)));
        }
    }

    /**
     * Tests for {@link TempoController#linearToFactor}
     */
    @Test
    public void testLinearToFactorHighest() {
        assertThat(TempoController.linearToFactor(HIGHEST_SLIDER_VALUE), is(HIGHEST_TEMPO_FACTOR));
    }

    /**
     * Tests for {@link TempoController#linearToFactor}
     */
    @Test
    public void testLinearToFactorMidpoint() {
        assertThat(TempoController.linearToFactor(DEFAULT_SLIDER_VALUE), is(DEFAULT_TEMPO_FACTOR));
    }

    /**
     * Tests for {@link TempoController#linearToFactor}
     */
    @Test
    public void testLinearToFactorLowest() {
        assertThat(TempoController.linearToFactor(LOWEST_SLIDER_VALUE), is(LOWEST_TEMPO_FACTOR));
    }

    /**
     * Tests for {@link TempoController#factorToLinear}
     */
    @Test
    public void testFactorToLinearHighest() {
        assertThat(TempoController.factorToLinear(HIGHEST_TEMPO_FACTOR), is(HIGHEST_SLIDER_VALUE));
    }

    /**
     * Tests for {@link TempoController#factorToLinear}
     */
    @Test
    public void testFactorToLinearMidpoint() {
        assertThat(TempoController.factorToLinear(DEFAULT_TEMPO_FACTOR), is(DEFAULT_SLIDER_VALUE));
    }

    /**
     * Tests for {@link TempoController#factorToLinear}
     */
    @Test
    public void testFactorToLinearLowest() {
        assertThat(TempoController.factorToLinear(LOWEST_TEMPO_FACTOR), is(LOWEST_SLIDER_VALUE));
    }
}
