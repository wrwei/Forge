package sranger.clock;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, in seconds, advanced by the system framework (SR-Var1).
 */
public final class Clock {

    @RoboChartType("real") private double currentTime = 0.0;

    /**
     * Advances time by the given non-negative number of seconds; time never runs backwards.
     */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            this.currentTime = this.currentTime + seconds;
        }
    }

    /** Current time in seconds. */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }
}
