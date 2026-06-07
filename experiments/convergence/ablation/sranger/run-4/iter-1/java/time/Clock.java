package sranger.time;

import sranger.annotation.RoboChartType;

/**
 * Time source for the controller's timed Turning → Moving transition.
 * The framework advances the clock each control cycle.
 */
public final class Clock {

    @RoboChartType("real")
    private double nowMs;

    /** Advances the clock to the given time. */
    public void setNowMs(@RoboChartType("real") double value) {
        this.nowMs = value;
    }

    /** Current time. */
    public double nowMs() {
        return nowMs;
    }
}
