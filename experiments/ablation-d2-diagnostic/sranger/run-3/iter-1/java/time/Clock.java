package sranger.time;

import sranger.annotation.RoboChartType;

/**
 * Time source for the controller, advanced by the system framework (milliseconds).
 */
public final class Clock {

    @RoboChartType("real")
    private double currentMs;

    /** Current time in milliseconds. */
    @RoboChartType("real")
    public double nowMs() {
        return currentMs;
    }

    /** Advances the clock by a non-negative number of milliseconds. */
    public void advanceMs(@RoboChartType("real") double elapsedMs) {
        if (elapsedMs > 0.0) {
            this.currentMs = this.currentMs + elapsedMs;
        }
    }
}
