package sranger.controller;

import sranger.annotation.RoboChartType;

/**
 * Monotonic time base of the controller, in seconds, advanced by the system
 * framework once per control cycle.
 */
public final class Clock {

    @RoboChartType("real")
    private double currentTime = 0.0;

    /** Returns the current time in seconds. */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }

    /** Moves the time forward by {@code seconds}; non-positive values are ignored. */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            this.currentTime = this.currentTime + seconds;
        }
    }
}
