package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/** Monotonic logical time source, advanced by the platform each control cycle. */
public final class Clock {

    @RoboChartType("nat")
    private long now;

    /** Current time in abstract time units. */
    @RoboChartType("nat")
    public long nowMs() {
        return now;
    }

    /** Advances time by {@code delta} units; non-positive deltas are ignored. */
    public void advance(@RoboChartType("nat") long delta) {
        if (delta > 0) {
            now += delta;
        }
    }
}
