package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Time source for the controller's timed Turning to Moving transition.
 * The controller records {@code now()} into its clock-reset variable on
 * entry to Turning.
 */
public final class Clock {

    @RoboChartType("real")
    private double currentTime;

    /** Sets the current time (seconds). */
    public void set(@RoboChartType("real") double seconds) {
        this.currentTime = seconds;
    }

    /** Current time in seconds. */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }
}
