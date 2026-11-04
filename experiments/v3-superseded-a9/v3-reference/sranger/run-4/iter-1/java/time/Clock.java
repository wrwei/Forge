package sranger.time;

import sranger.annotation.RoboChartType;

/**
 * Monotonic time source for the controller's timed transitions.
 */
public final class Clock {

    @RoboChartType("real") private final double originSeconds;

    /** Starts the clock at zero. */
    public Clock() {
        this.originSeconds = System.nanoTime() / 1.0e9;
    }

    /**
     * Current time.
     *
     * @return seconds elapsed since this clock was created
     */
    @RoboChartType("real")
    public double now() {
        return System.nanoTime() / 1.0e9 - originSeconds;
    }
}
