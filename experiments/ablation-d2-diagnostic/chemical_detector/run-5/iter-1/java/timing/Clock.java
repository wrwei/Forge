package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/** Monotonic time source, in model time units, advanced by the platform. */
public final class Clock {

    @RoboChartType("nat")
    private long now;

    /** Current time. */
    @RoboChartType("nat")
    public long nowMs() {
        return now;
    }

    /** Advances time by {@code units}. */
    public void advance(@RoboChartType("nat") long units) {
        this.now = now + units;
    }
}
