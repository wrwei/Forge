package sranger.controller;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, in seconds. The system framework advances it; the controller
 * reads it to reset and test clockResetTime (SR-Var1, SR-GP2).
 */
public final class Clock {

    @RoboChartType("real") private double currentTime = 0.0;

    /** Advances the time by the given number of seconds; a non-positive amount is ignored. */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            this.currentTime = this.currentTime + seconds;
        }
    }

    /** The current time, in seconds. */
    @RoboChartType("real") public double now() { return currentTime; }
}
