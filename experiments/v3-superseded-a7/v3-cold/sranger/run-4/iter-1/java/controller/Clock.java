package sranger.controller;

import sranger.annotation.RoboChartType;

/**
 * Time source of the SRanger controller, in seconds. Time starts at zero and only
 * moves forward; the system framework advances it.
 */
public final class Clock {
    @RoboChartType("real") private double currentTime = 0.0;

    /** Advances the clock by the given number of seconds; a non-positive amount is ignored. */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            currentTime = currentTime + seconds;
        }
    }

    /** Returns the current time in seconds. */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }
}
