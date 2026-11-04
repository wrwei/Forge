package sranger.controller;

import sranger.annotation.RoboChartType;

/**
 * Time base of the SRanger controller, used for the clock-reset variable
 * clockResetTime and the timed Turning to Moving transition (SR-Var1, SR-GP2).
 * Time is measured in seconds from the creation of the clock.
 */
public final class Clock {

    @RoboChartType("real") private final double originSeconds = System.nanoTime() / 1.0e9;

    /** Current time in seconds since the clock was created (non-negative). */
    @RoboChartType("real")
    public double now() {
        return System.nanoTime() / 1.0e9 - originSeconds;
    }
}
