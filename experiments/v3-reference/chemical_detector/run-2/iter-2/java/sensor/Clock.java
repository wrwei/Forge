package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/**
 * The time source of the movement subsystem (CD-MV-Clock1), counted in control cycles.
 */
public final class Clock {

    @RoboChartType("nat") private long ticks = 0;

    /** The number of control cycles elapsed since start-up. */
    @RoboChartType("nat")
    public long now() {
        return ticks;
    }

    /** Advances time by one control cycle. */
    public void advance() {
        ticks = ticks + 1;
    }
}
