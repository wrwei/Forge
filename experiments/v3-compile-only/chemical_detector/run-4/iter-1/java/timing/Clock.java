package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/**
 * Discrete time source of the movement subsystem (CD-MV-Clock1). Time is
 * counted in whole time units; the platform calls {@link #tick()} once per
 * unit, and {@link #pause(long)} models a bounded wait of the controller.
 */
public final class Clock {

    @RoboChartType("nat") private long ticks = 0L;

    /** Advances time by one unit. */
    public void tick() {
        ticks = ticks + 1L;
    }

    /** The current time, in time units since start-up. */
    @RoboChartType("nat")
    public long now() {
        return ticks;
    }

    /** Waits for the given number of time units (time advances by that much). */
    public void pause(@RoboChartType("nat") long duration) {
        ticks = ticks + duration;
    }
}
