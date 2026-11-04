package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, in seconds since start-up.
 */
public final class Clock {

    @RoboChartType("real") private double elapsedSeconds = 0.0;

    /** Moves the clock forward by the given number of seconds; time never runs backwards. */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            elapsedSeconds = elapsedSeconds + seconds;
        }
    }

    /** The current time in seconds. */
    @RoboChartType("real")
    public double now() {
        return elapsedSeconds;
    }
}
