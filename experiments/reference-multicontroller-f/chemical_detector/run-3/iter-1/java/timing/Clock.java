package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/**
 * Manually advanced time source, in the controller's abstract time unit.
 */
public final class Clock {

    @RoboChartType("nat")
    private long now;

    /** Current time. */
    @RoboChartType("nat")
    public long nowMs() {
        return now;
    }

    /** Advances time by {@code units}. */
    public void advance(@RoboChartType("nat") long units) {
        this.now = now + units;
    }
}
