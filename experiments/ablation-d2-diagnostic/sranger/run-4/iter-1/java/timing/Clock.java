package sranger.timing;

import sranger.annotation.RoboChartType;

/**
 * Time source for the controller's timed transitions. Time is reported in the
 * same unit as the controller's duration constants (seconds).
 */
public final class Clock {

    @RoboChartType("real")
    private double now;

    /** Current time, in the unit of the controller's duration constants. */
    @RoboChartType("real")
    public double nowMs() {
        return now;
    }

    /** Advances the clock by a non-negative amount of time. */
    public void advance(@RoboChartType("real") double elapsed) {
        if (elapsed > 0.0) {
            this.now = this.now + elapsed;
        }
    }
}
