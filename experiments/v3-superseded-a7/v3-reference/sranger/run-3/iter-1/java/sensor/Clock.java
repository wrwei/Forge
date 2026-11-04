package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Monotonic time source of the controller, in seconds since this clock was
 * created (supports the clock variable of SR-Var1).
 */
public final class Clock {

    @RoboChartType("real") private static final double NANOS_PER_SECOND = 1000000000.0;

    @RoboChartType("real") private final double originSeconds;

    /** Starts the clock at zero. */
    public Clock() {
        this.originSeconds = System.nanoTime() / NANOS_PER_SECOND;
    }

    /** The current time in seconds, non-negative and non-decreasing. */
    @RoboChartType("real")
    public double now() {
        return System.nanoTime() / NANOS_PER_SECOND - originSeconds;
    }
}
