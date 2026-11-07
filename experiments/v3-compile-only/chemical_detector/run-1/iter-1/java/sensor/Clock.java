package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/**
 * Logical time source of the movement subsystem (CD-MV-Clock1). Time is
 * counted in whole time units; the environment advances it, and a wait by
 * the controller lets the waited units pass.
 */
public final class Clock {

    @RoboChartType("nat") private long ticks = 0;

    /** The current time, in time units. */
    @RoboChartType("nat")
    public long now() {
        return ticks;
    }

    /** Waits for the given number of time units (CD-Const3, CD-Const6). */
    public void pause(@RoboChartType("nat") long duration) {
        this.ticks = ticks + duration;
    }

    /** Lets the given number of time units pass in the environment. */
    public void advance(@RoboChartType("nat") long duration) {
        this.ticks = ticks + duration;
    }
}
