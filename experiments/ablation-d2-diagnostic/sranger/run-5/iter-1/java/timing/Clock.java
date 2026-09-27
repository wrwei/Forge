package sranger.timing;

import sranger.annotation.RoboChartType;

/**
 * Time source of the SRanger controller, advanced by the system framework.
 * The controller time base is seconds (SR-Var1, SR-DM2 turnDuration).
 */
public final class Clock {

    @RoboChartType("real")
    private double now;

    /**
     * Current time in the controller time base (seconds). The method name follows the
     * pipeline's clock convention.
     */
    @RoboChartType("real")
    public double nowMs() {
        return now;
    }

    /**
     * Advances the time by the given non-negative duration in seconds.
     */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            this.now = this.now + seconds;
        }
    }
}
