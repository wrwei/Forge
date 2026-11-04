package sranger.timing;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, in seconds since start-up. The controller
 * reads it to reset and test the Turning timer (SR-Var1, SR-GP2).
 */
public final class Clock {

    @RoboChartType("real") private double currentTime = 0.0;

    /** @return the current time in seconds */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }

    /**
     * Moves time forward; non-positive amounts are ignored.
     *
     * @param seconds elapsed time in seconds
     */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            this.currentTime = this.currentTime + seconds;
        }
    }
}
