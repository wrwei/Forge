package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/**
 * Platform time source. Time is advanced explicitly by the platform once per
 * elapsed time unit, so controller behaviour is reproducible.
 */
public final class Clock {

    @RoboChartType("nat")
    private long now;

    /** Current platform time, in platform time units. */
    @RoboChartType("nat")
    public long nowMs() {
        return now;
    }

    /** Advances platform time by {@code elapsed} units; negative values are ignored. */
    public void advance(@RoboChartType("nat") long elapsed) {
        if (elapsed > 0) {
            this.now = this.now + elapsed;
        }
    }
}
