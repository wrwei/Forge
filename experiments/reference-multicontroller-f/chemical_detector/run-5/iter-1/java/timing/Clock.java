package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/** Monotonic controller clock, advanced by the platform. Time is measured in clock units. */
public final class Clock {

    @RoboChartType("nat")
    private long elapsed;

    /** Current time in clock units. */
    @RoboChartType("nat")
    public long nowMs() {
        return elapsed;
    }

    /** Advances the clock by the given number of units. */
    public void advance(@RoboChartType("nat") long units) {
        this.elapsed = elapsed + units;
    }
}
