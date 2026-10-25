package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/**
 * Platform time source, advanced by the platform once per control cycle.
 */
public final class Clock {

    @RoboChartType("nat")
    private long now;

    /** Current platform time, in the same units as the timing constants. */
    @RoboChartType("nat")
    public long nowMs() {
        return now;
    }

    /** Advances platform time by {@code elapsed} units. */
    public void advance(@RoboChartType("nat") long elapsed) {
        this.now = now + elapsed;
    }
}
