package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** Time source of the movement subsystem, in whole time units (CD-MV-Clock1). */
public final class Clock {
    @RoboChartType("nat") private long ticks = 0;

    /** Advances time by one unit. */
    public void tick() {
        this.ticks = ticks + 1;
    }

    /** The current time. */
    @RoboChartType("nat")
    public long now() {
        return ticks;
    }
}
