package sranger.controller;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, in seconds. The system framework advances it
 * once per control cycle.
 */
public final class Clock {

    @RoboChartType("real") private double currentTime = 0.0;

    /**
     * Moves time forward; a non-positive amount leaves it unchanged.
     *
     * @param seconds elapsed time since the previous advance
     */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            this.currentTime = this.currentTime + seconds;
        }
    }

    /** The current time in seconds. */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }
}
