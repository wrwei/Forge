package sranger.controller;

import sranger.annotation.RoboChartType;

/**
 * Time source of the SRanger controller, in seconds. Time starts at 0 and is
 * moved forward by the system framework.
 */
public final class Clock {

    @RoboChartType("real") private double currentTime = 0.0;

    /** Current time in seconds. */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }

    /** Moves the clock forward by the given number of seconds; time never goes back. */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            this.currentTime = this.currentTime + seconds;
        }
    }
}
