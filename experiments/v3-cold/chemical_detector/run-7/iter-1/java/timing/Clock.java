package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/** Logical time in ticks, advanced by the platform and by the controller's waits. */
public final class Clock {

    @RoboChartType("nat") private long ticks = 0;

    /** The current time. */
    @RoboChartType("nat")
    public long now() {
        return ticks;
    }

    /** Advances time by one tick. */
    public void tick() {
        ticks = ticks + 1;
    }

    /** Waits for the given number of ticks. */
    public void delay(@RoboChartType("nat") long duration) {
        ticks = ticks + duration;
    }
}
