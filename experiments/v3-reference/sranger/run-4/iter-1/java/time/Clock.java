package sranger.time;

import sranger.annotation.RoboChartType;

/**
 * Monotonic time source of the controller.
 */
public final class Clock {

    @RoboChartType("real") private final double originSeconds = System.nanoTime() / 1.0e9;

    /** Current time in seconds, counted from the creation of this clock. */
    @RoboChartType("real")
    public double now() {
        return System.nanoTime() / 1.0e9 - originSeconds;
    }
}
