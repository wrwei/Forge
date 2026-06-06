package sranger.clock;

import sranger.annotation.RoboChartType;

/**
 * System clock used by the SRanger controller for the timed Turning → Moving
 * transition (SR-Var1, SR-GP2).
 *
 * The controller calls nowMs() to record clockResetTime when it enters Turning,
 * and again on subsequent ticks to test whether the elapsed time has reached
 * the configured turn duration.
 */
public final class Clock {

    /**
     * Current monotonic time, in milliseconds since some fixed epoch.
     * The downstream M2M rewrites `clock.nowMs() - clockField` to
     * `since(clockField)` in the RoboChart model.
     */
    @RoboChartType("real")
    public double nowMs() {
        return System.currentTimeMillis();
    }
}
