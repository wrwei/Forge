package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/**
 * Logical time in whole time units, advanced once per control cycle and by
 * every pause.
 */
public final class Clock {

    @RoboChartType("nat") private long ticks = 0;

    /**
     * The current time.
     */
    @RoboChartType("nat")
    public long now() {
        return ticks;
    }

    /**
     * Advances time by one unit (one control cycle).
     */
    public void tick() {
        ticks = ticks + 1;
    }

    /**
     * Waits for the given number of time units.
     */
    public void pause(@RoboChartType("nat") long duration) {
        ticks = ticks + duration;
    }
}
