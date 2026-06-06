package sranger.clock;

import sranger.annotation.RoboChartType;

/**
 * Wall-clock source for the SRanger controller. Exposes the current time in
 * seconds since some fixed reference. Used by the controller to compute the
 * elapsed time since clockResetTime for the autonomous Turning -> Moving
 * transition.
 *
 * The class is named Clock so that the M2M ETL recognises it as a RoboChart
 * clock dependency without an explicit annotation.
 */
public final class Clock {

    @RoboChartType("real")
    private double now = 0.0;

    /** Advance/replace the current time (seconds). */
    public void set(@RoboChartType("real") double seconds) {
        this.now = seconds;
    }

    /** Current time in seconds. */
    @RoboChartType("real")
    public double nowSec() {
        return now;
    }
}
