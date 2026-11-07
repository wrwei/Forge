package sranger.controller;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, in seconds. The system framework advances
 * it once per control cycle; it starts at 0.0 and never goes backwards.
 */
public final class Clock {

    @RoboChartType("real") private double currentTime = 0.0;

    /** Current time in seconds. */
    @RoboChartType("real") public double now() {
        return currentTime;
    }

    /** Advances the time by the given number of seconds; non-positive values are ignored. */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            currentTime = currentTime + seconds;
        }
    }
}
