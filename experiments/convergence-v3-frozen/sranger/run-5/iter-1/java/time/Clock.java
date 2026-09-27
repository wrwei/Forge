package sranger.time;

import sranger.annotation.RoboChartType;

/**
 * Wall-clock source for the timed Turning to Moving transition (SR-Var1).
 * The controller records readings of this clock in clockResetTime.
 */
public final class Clock {

    /** Current time in seconds. */
    @RoboChartType("real")
    public double nowSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }
}
