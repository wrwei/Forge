package sranger.time;

import sranger.annotation.RoboChartType;

/**
 * Monotonic time source backing the controller's clock-reset variable (SR-Var1).
 */
public final class Clock {

    /** Current time in seconds. */
    @RoboChartType("real")
    public double nowSeconds() {
        return System.nanoTime() / 1000000000.0;
    }
}
