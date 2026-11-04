package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** Time source for the movement subsystem's evasion timing (CD-MV-Clock1). */
public final class Clock {
    @RoboChartType("nat") private long time = 0;

    /** The current time, in time units. */
    @RoboChartType("nat")
    public long now() {
        return time;
    }

    /** Lets the given number of time units pass. */
    public void advance(@RoboChartType("nat") long units) {
        this.time = time + units;
    }
}
