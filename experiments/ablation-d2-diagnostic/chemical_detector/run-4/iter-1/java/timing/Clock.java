package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/**
 * Monotonic time source for the movement subsystem's evasion clock (CD-MV-Clock1).
 * Time is advanced by the platform once per control cycle.
 */
public final class Clock {

    @RoboChartType("nat")
    private long now;

    /** Current time. */
    @RoboChartType("nat")
    public long nowMs() {
        return now;
    }

    /** Advances time by a non-negative amount. */
    public void advance(@RoboChartType("nat") long elapsed) {
        if (elapsed > 0) {
            now = now + elapsed;
        }
    }
}
