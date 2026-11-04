package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, advanced by the system framework.
 * Time starts at zero and never decreases.
 */
public final class Clock {

    @RoboChartType("real") private double currentTime = 0.0;

    /**
     * Returns the current time.
     *
     * @return time, seconds
     */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }

    /**
     * Advances the clock. A negative or NaN amount is ignored.
     *
     * @param seconds elapsed time, seconds
     */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            this.currentTime = this.currentTime + seconds;
        }
    }
}
