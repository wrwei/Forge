package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Time source for the controller's timed Turning-to-Moving transition.
 */
public final class Clock {

    @RoboChartType("real")
    private double currentTime;

    /** Advances the clock by the given number of seconds. */
    public void advance(@RoboChartType("real") double seconds) {
        this.currentTime = this.currentTime + seconds;
    }

    /** Current time in seconds. */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }
}
