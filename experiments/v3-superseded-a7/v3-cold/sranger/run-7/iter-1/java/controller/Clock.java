package sranger.controller;

import sranger.annotation.RoboChartType;

/**
 * Time source for the controller, in seconds. The system framework advances it.
 */
public final class Clock {

    @RoboChartType("real") private double currentTime = 0.0;

    /** Advances the clock to the given time; a time earlier than the current one is ignored. */
    public void advanceTo(@RoboChartType("real") double seconds) {
        if (seconds > currentTime) {
            this.currentTime = seconds;
        }
    }

    /** The current time in seconds. */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }
}
