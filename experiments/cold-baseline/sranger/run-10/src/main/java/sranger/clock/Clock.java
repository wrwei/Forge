package sranger.clock;

import sranger.annotation.RoboChartType;

/**
 * System clock dependency. Exposes the current monotonic time in seconds.
 * Class is named Clock so the ETL recognises it automatically.
 */
public final class Clock {

    @RoboChartType("real")
    private double now;

    public Clock() {
        this.now = 0.0;
    }

    /** Advance the clock by delta seconds. */
    public void advance(@RoboChartType("real") double delta) {
        this.now = this.now + delta;
    }

    /** Set the clock directly (testing/simulation hook). */
    public void setNow(@RoboChartType("real") double now) {
        this.now = now;
    }

    @RoboChartType("real")
    public double now() {
        return this.now;
    }
}
