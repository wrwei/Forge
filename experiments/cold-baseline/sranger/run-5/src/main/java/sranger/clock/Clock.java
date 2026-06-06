package sranger.clock;

import sranger.annotation.RoboChartType;

/**
 * System clock providing the current time for time-based transitions.
 * The M2M recognises the class simple name "Clock" and a field of this type
 * on the controller as a RoboChart clock dependency.
 */
public final class Clock {

    @RoboChartType("real")
    private double now = 0.0;

    /** The current time in seconds. */
    @RoboChartType("real")
    public double nowMs() {
        return now;
    }

    /** Advance the clock by the given delta (called by the framework). */
    public void advance(@RoboChartType("real") double delta) {
        this.now = this.now + delta;
    }
}
