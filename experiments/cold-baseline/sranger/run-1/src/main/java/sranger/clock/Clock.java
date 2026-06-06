package sranger.clock;

import sranger.annotation.RoboChartType;

/**
 * System clock. Exposes the elapsed time since power-up in seconds. Used by
 * the controller to record clockResetTime on entering Turning and to
 * evaluate the turnDurationElapsed guard.
 *
 * Named "Clock" so the M2M auto-detects it as a clock dependency.
 */
public final class Clock {

    @RoboChartType("real")
    private double now = 0.0;

    /** Advance the clock to the given absolute time (seconds). */
    public void setNow(@RoboChartType("real") double now) {
        this.now = now;
    }

    /** Current time in seconds since power-up. */
    @RoboChartType("real")
    public double now() {
        return now;
    }
}
