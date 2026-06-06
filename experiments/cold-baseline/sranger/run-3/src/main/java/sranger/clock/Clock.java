package sranger.clock;

import sranger.annotation.RoboChartType;

/**
 * System clock dependency for the SRanger controller.
 * Exposes the current time in milliseconds; the controller
 * uses this to implement the timed Turning -> Moving transition.
 */
public final class Clock {

    @RoboChartType("real")
    public double nowMs() {
        return System.currentTimeMillis();
    }
}
