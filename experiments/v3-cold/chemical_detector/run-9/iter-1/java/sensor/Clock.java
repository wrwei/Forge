package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** Time source of the movement subsystem, in time units (CD-MV-Clock1). */
public final class Clock {
    @RoboChartType("nat") private long ticks = 0;

    /** The current time. */
    @RoboChartType("nat")
    public long now() {
        return ticks;
    }

    /** Advances the time by the given number of time units. */
    public void advance(@RoboChartType("nat") long elapsed) {
        this.ticks = ticks + elapsed;
    }
}
