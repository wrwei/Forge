package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/**
 * Monotonic time source for the movement subsystem's evasion timing. Time is
 * discrete and advanced by the platform; the timing constants in
 * {@link chemical_detector.constants.ChemConstants} use the same unit.
 */
public final class Clock {

    @RoboChartType("nat")
    private long now;

    /** Current time. */
    @RoboChartType("nat")
    public long nowMs() {
        return now;
    }

    /** Advances time by {@code elapsed} units. */
    public void advance(@RoboChartType("nat") long elapsed) {
        now = now + elapsed;
    }
}
