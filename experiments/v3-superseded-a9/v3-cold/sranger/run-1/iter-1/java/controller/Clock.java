package sranger.controller;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, in seconds. The framework advances it.
 */
public final class Clock {

    @RoboChartType("real") private double currentTime = 0.0;

    /**
     * Moves time forward; a non-positive amount leaves the time unchanged.
     *
     * @param seconds the time elapsed since the last advance, seconds
     */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            this.currentTime = this.currentTime + seconds;
        }
    }

    /** The current time, seconds. */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }
}
