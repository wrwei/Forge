package sranger.controller;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, advanced by the system framework. Time
 * starts at zero and never decreases.
 */
public final class Clock {

    @RoboChartType("real")
    private double currentTime = 0.0;

    /**
     * Current time.
     *
     * @return the time in seconds since start-up
     */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }

    /**
     * Advances the clock; a non-positive amount leaves it unchanged.
     *
     * @param seconds the elapsed time in seconds
     */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            this.currentTime = this.currentTime + seconds;
        }
    }
}
