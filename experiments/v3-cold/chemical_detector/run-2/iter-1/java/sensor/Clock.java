package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** Discrete time source of the movement subsystem, in ticks. */
public final class Clock {
    @RoboChartType("nat") private long ticks = 0;

    /**
     * The current time.
     *
     * @return ticks elapsed since start-up
     */
    @RoboChartType("nat")
    public long now() {
        return ticks;
    }

    /**
     * Blocks for the given number of ticks (used for evadeTime and outPeriod).
     *
     * @param duration ticks to wait
     */
    public void pause(@RoboChartType("nat") long duration) {
        ticks = ticks + duration;
    }
}
