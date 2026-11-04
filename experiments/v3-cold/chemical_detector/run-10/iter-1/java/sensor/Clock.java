package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** Discrete time source of the movement subsystem (CD-MV-Clock1). */
public final class Clock {

    @RoboChartType("nat") private long ticks = 0L;

    /** The current time, in time units since start-up. */
    @RoboChartType("nat")
    public long now() {
        return ticks;
    }

    /** Advances time by one unit; called once per control cycle. */
    public void tick() {
        ticks = ticks + 1;
    }

    /** Waits the given number of time units: the caller is busy until they have passed. */
    public void pause(@RoboChartType("nat") int duration) {
        ticks = ticks + duration;
    }
}
