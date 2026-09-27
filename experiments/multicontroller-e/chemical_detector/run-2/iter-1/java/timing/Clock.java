package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/** Monotonic time source used for the movement subsystem's evasion clock (CD-MV-Clock1). */
public final class Clock {

    @RoboChartType("nat")
    private long now;

    /** Current time in clock units (milliseconds on a real platform). */
    @RoboChartType("nat")
    public long nowMs() {
        return now;
    }

    /** Lets {@code units} of time pass. */
    public void advance(@RoboChartType("nat") long units) {
        if (units < 0) {
            throw new IllegalArgumentException("time cannot run backwards: " + units);
        }
        now = now + units;
    }
}
