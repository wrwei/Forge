package sranger.timing;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, advanced by the system framework.
 */
public final class Clock {

    @RoboChartType("real") private double currentTime = 0.0;

    /** @return the current time (s) */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }

    /**
     * Advances the current time. Time never runs backwards: a non-positive or
     * NaN step is ignored.
     *
     * @param seconds elapsed time since the last advance (s)
     */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            currentTime = currentTime + seconds;
        }
    }
}
