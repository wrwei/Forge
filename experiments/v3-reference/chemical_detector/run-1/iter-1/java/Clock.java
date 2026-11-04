package chemical_detector;

import chemical_detector.annotation.RoboChartType;

/**
 * Simulated time source of the movement subsystem.
 */
public final class Clock {
    @RoboChartType("nat") private long time = 0;

    /** Current time (time units). */
    @RoboChartType("nat")
    public long now() {
        return time;
    }

    /** Lets the given amount of time pass (environment tick). */
    public void advance(@RoboChartType("nat") long duration) {
        time = time + duration;
    }

    /** Waits for the given duration before the behaviour continues. */
    public void pause(@RoboChartType("nat") long duration) {
        time = time + duration;
    }
}
