package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/**
 * Logical time source of the movement subsystem, in ticks. The platform
 * advances it with {@link #tick()}; a controller that waits advances it with
 * {@link #delay(long)}.
 */
public final class Clock {

    @RoboChartType("nat") private long ticks = 0;

    /**
     * The current time.
     *
     * @return ticks elapsed since startup
     */
    public long now() {
        return ticks;
    }

    /** Advances time by one tick. */
    public void tick() {
        ticks = ticks + 1;
    }

    /**
     * Waits for a bounded number of ticks.
     *
     * @param duration the number of ticks to wait
     */
    public void delay(@RoboChartType("nat") long duration) {
        ticks = ticks + duration;
    }
}
