package sranger.time;

import sranger.annotation.RoboChartType;

/**
 * Time source for the controller's timed Turning to Moving transition
 * (SR-Var1, SR-GP2). The system framework advances it each control cycle.
 */
public final class Clock {

    @RoboChartType("real")
    private double currentTime = 0.0;

    /**
     * Advances the clock.
     *
     * @param seconds elapsed time to add (ignored if negative)
     */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            this.currentTime = this.currentTime + seconds;
        }
    }

    /**
     * Returns the current time.
     *
     * @return the current time in seconds
     */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }
}
