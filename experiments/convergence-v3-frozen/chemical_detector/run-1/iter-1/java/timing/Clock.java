package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/**
 * Monotonic time source. Controller fields assigned from {@link #nowMs()}
 * become RoboChart clocks, and time-elapsed guards written against them
 * become {@code since(...)} expressions.
 */
public final class Clock {

    @RoboChartType("nat")
    public long nowMs() {
        return System.nanoTime() / 1000000L;
    }
}
