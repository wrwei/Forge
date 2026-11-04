package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Monotonic time source of the SRanger controller, in seconds since construction.
 */
public final class Clock {

    @RoboChartType("real") private final double originSeconds = System.nanoTime() / 1.0e9;

    /**
     * Returns the current time in seconds.
     *
     * @return non-negative time in seconds
     */
    @RoboChartType("real")
    public double now() {
        return System.nanoTime() / 1.0e9 - originSeconds;
    }
}
