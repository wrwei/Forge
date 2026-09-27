package sranger.time;

import sranger.annotation.RoboChartType;

/** Controller time base in milliseconds, advanced by the framework. */
public final class Clock {

    @RoboChartType("real")
    private double currentMs;

    /** Advances time by the given number of milliseconds (ignored if not positive). */
    public void advanceMs(@RoboChartType("real") double ms) {
        if (ms > 0.0) {
            this.currentMs = this.currentMs + ms;
        }
    }

    /** Current time in milliseconds. */
    @RoboChartType("real")
    public double nowMs() {
        return currentMs;
    }
}
