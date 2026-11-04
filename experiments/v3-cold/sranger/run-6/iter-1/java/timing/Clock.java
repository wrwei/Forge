package sranger.timing;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, in seconds; advanced by the system framework.
 */
public final class Clock {
    @RoboChartType("real") private double currentTime = 0.0;

    /** The current time in seconds. */
    @RoboChartType("real") public double now() {
        return currentTime;
    }

    /** Moves time forward; a non-positive amount leaves it unchanged. */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            this.currentTime = currentTime + seconds;
        }
    }
}
