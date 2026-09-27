package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** Monotonic time source for the movement subsystem's evasion timer. */
public final class Clock {

    @RoboChartType("nat")
    private long now;

    /** Current time. */
    @RoboChartType("nat")
    public long nowMs() {
        return now;
    }

    /** Advances time by the given amount. */
    public void advance(@RoboChartType("nat") long amount) {
        this.now = this.now + amount;
    }
}
