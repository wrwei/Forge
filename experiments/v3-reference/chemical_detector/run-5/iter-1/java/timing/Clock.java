package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/**
 * Time source of the movement subsystem, counted in whole time units.
 */
public final class Clock {

    @RoboChartType("nat")
    private long ticks = 0;

    /**
     * The current time.
     *
     * @return the number of time units elapsed since start-up
     */
    @RoboChartType("nat")
    public long now() {
        return ticks;
    }

    /**
     * Lets time pass in the environment.
     *
     * @param elapsed the number of time units that have passed
     */
    public void advance(@RoboChartType("nat") long elapsed) {
        ticks = ticks + elapsed;
    }

    /**
     * Waits for a bounded number of time units before the caller continues.
     *
     * @param duration the number of time units to wait
     */
    public void delay(@RoboChartType("nat") long duration) {
        ticks = ticks + duration;
    }
}
