package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/**
 * Monotonic time source advanced by the platform's control loop.
 */
public final class Clock {

    @RoboChartType("nat")
    private long now;

    @RoboChartType("nat")
    public long nowMs() {
        return now;
    }

    public void advance(@RoboChartType("nat") long elapsed) {
        now = now + elapsed;
    }
}
