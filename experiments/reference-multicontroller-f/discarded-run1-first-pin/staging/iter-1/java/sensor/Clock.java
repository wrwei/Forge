package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/**
 * Monotonic time source for the movement subsystem's evasion timing.
 * The host advances it once per control cycle.
 */
public final class Clock {

    @RoboChartType("nat")
    private long now;

    /** Current time. */
    @RoboChartType("nat")
    public long nowMs() {
        return now;
    }

    /** Advances the current time by {@code elapsed}. */
    public void advance(@RoboChartType("nat") long elapsed) {
        this.now = now + elapsed;
    }
}
