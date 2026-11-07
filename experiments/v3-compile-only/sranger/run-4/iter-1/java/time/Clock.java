package sranger.time;

import sranger.annotation.RoboChartType;

/**
 * Monotonic time source of the controller, in seconds since power-up. The
 * system framework advances it each control cycle.
 */
public final class Clock {

    @RoboChartType("real") private double currentTime = 0.0;

    /**
     * Advances the clock. Time never runs backwards: a negative amount is ignored.
     *
     * @param elapsedSeconds the time elapsed since the previous advance, in seconds
     */
    public void advance(@RoboChartType("real") double elapsedSeconds) {
        if (elapsedSeconds > 0.0) {
            this.currentTime = this.currentTime + elapsedSeconds;
        }
    }

    /**
     * @return the current time in seconds
     */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }
}
