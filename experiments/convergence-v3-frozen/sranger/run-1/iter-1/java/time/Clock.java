package sranger.time;

import sranger.annotation.RoboChartType;

/**
 * Wall-clock source for the controller's timed transition (SR-Var1). Reads
 * taken through this class are what the model extraction promotes to a
 * RoboChart clock.
 */
public final class Clock {

    /** Current time in seconds. */
    @RoboChartType("real")
    public double nowSeconds() {
        return System.nanoTime() / 1_000_000_000.0;
    }
}
