package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/**
 * Logical time in ticks. The environment advances it once per control cycle;
 * {@link #delay} models the controller waiting a number of ticks.
 */
public final class Clock {

    @RoboChartType("nat") private long ticks = 0;

    /** Current time in ticks. */
    @RoboChartType("nat")
    public long now() {
        return ticks;
    }

    /** Advances time by one tick. */
    public void tick() {
        this.ticks = this.ticks + 1;
    }

    /** Waits {@code duration} ticks. */
    public void delay(@RoboChartType("nat") long duration) {
        this.ticks = this.ticks + duration;
    }
}
