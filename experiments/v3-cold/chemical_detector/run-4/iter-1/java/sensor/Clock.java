package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/**
 * Discrete time source of the movement subsystem, counted in time units.
 */
public final class Clock {
    @RoboChartType("nat") private long ticks = 0;

    /**
     * @return the current time
     */
    @RoboChartType("nat")
    public long now() {
        return ticks;
    }

    /**
     * Lets time pass.
     *
     * @param elapsed the number of time units that have passed
     */
    public void advance(@RoboChartType("nat") long elapsed) {
        this.ticks = this.ticks + elapsed;
    }
}
