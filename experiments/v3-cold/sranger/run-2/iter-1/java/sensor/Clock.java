package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Monotonic time source of the SRanger controller, in seconds. The controller
 * records {@link #now()} in its clock-reset variable (SR-Var1) and measures the
 * time elapsed since then (SR-GP2).
 */
public final class Clock {

    @RoboChartType("real") private double currentTime = 0.0;

    /**
     * Moves time forward. A negative or non-numeric amount is ignored, so time
     * never runs backwards.
     *
     * @param seconds the time elapsed, seconds
     */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            this.currentTime = this.currentTime + seconds;
        }
    }

    /**
     * Returns the current time.
     *
     * @return the current time, seconds
     */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }
}
