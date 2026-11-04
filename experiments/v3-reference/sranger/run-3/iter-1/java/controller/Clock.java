package sranger.controller;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, advanced by the system framework.
 */
public final class Clock {

    @RoboChartType("real") private double elapsedSeconds = 0.0;

    /**
     * Advances the time by the given non-negative amount; a negative amount is ignored.
     *
     * @param seconds the time that has passed, in seconds
     */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            this.elapsedSeconds = this.elapsedSeconds + seconds;
        }
    }

    /** Returns the current time in seconds. */
    @RoboChartType("real")
    public double now() {
        return elapsedSeconds;
    }
}
