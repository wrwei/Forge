package sranger.timing;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, in seconds. Starts at zero and never runs
 * backwards.
 */
public final class Clock {

    @RoboChartType("real") private double currentTime = 0.0;

    /** The current time, in seconds. */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }

    /** Moves the clock forward to the given time, in seconds; an earlier time is ignored. */
    public void advanceTo(@RoboChartType("real") double seconds) {
        if (seconds > currentTime) {
            this.currentTime = seconds;
        }
    }
}
