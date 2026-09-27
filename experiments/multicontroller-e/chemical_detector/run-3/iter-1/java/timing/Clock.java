package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/**
 * Logical clock read by the movement controller for stuck detection. Time is
 * advanced explicitly by the platform integration.
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
