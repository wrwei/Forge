package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * System time source for the timed Turning to Moving transition
 * (SR-Var1). The controller records the time on entering Turning and
 * compares elapsed time against the configured turn duration.
 */
public final class Clock {

    @RoboChartType("real")
    private double currentTime;

    /** Current time in seconds. */
    public double now() {
        return currentTime;
    }

    /** Sets the current time (called by the system framework each cycle). */
    public void setTime(@RoboChartType("real") double t) {
        this.currentTime = t;
    }
}
