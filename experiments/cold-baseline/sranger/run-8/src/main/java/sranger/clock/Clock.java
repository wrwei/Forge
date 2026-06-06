package sranger.clock;

import sranger.annotation.RoboChartType;

/**
 * Monotonic clock used by the SRanger controller to time
 * the autonomous Turning -> Moving transition.
 *
 * The class name {@code Clock} matches the M2M convention
 * for clock dependencies.
 */
public final class Clock {

    /** Returns the current time in seconds. */
    @RoboChartType("real")
    public double nowSeconds() {
        return System.nanoTime() / 1_000_000_000.0;
    }

    /** Returns the current time in milliseconds. */
    @RoboChartType("nat")
    public long nowMs() {
        return System.nanoTime() / 1_000_000L;
    }
}
