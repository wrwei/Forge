package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/**
 * Logical time in ticks, advanced by the platform; the movement subsystem measures
 * the elapsed time of an evasion sequence against it (CD-MV-Clock1).
 */
public final class Clock {

    @RoboChartType("nat") private long ticks = 0;

    /** The current time. */
    @RoboChartType("nat")
    public long now() {
        return ticks;
    }

    /** Advances time by the given number of ticks. */
    public void advance(@RoboChartType("nat") long elapsed) {
        this.ticks = this.ticks + elapsed;
    }
}
