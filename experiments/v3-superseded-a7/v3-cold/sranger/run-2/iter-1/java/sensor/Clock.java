package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, used to record and test clockResetTime
 * (SR-Var1, SR-GP2).
 */
public final class Clock {

    @RoboChartType("real") private final double startSeconds = System.nanoTime() / 1.0e9;

    /** The time in seconds since this clock was created. */
    @RoboChartType("real")
    public double now() {
        return System.nanoTime() / 1.0e9 - startSeconds;
    }
}
