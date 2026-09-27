package sranger.timing;

import sranger.annotation.RoboChartType;

/**
 * Controller time source. Time is expressed in seconds, the unit of SR-Var1
 * (clockResetTime) and SR-DM2 (turnDuration); the accessor keeps the name
 * nowMs() that the model extractor recognises for clock reads.
 */
public final class Clock {

    @RoboChartType("real")
    private double now;

    /** Advances the clock by the given non-negative number of seconds. */
    public void advance(@RoboChartType("real") double seconds) {
        this.now = this.now + Math.max(0.0, seconds);
    }

    /** Current controller time in seconds. */
    @RoboChartType("real")
    public double nowMs() {
        return now;
    }
}
