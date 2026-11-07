package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/** Monotonic time source counted in discrete time units. */
public final class Clock {
    @RoboChartType("nat") private long ticks = 0;

    /** Current time, in time units since start-up. */
    @RoboChartType("nat")
    public long now() {
        return ticks;
    }

    /** Advances time by one unit. */
    public void tick() {
        ticks = ticks + 1;
    }
}
