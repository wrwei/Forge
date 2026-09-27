package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/** Platform time base used to time evasion sequences (CD-MV-Clock1). */
public final class Clock {

    @RoboChartType("nat")
    private long now;

    /** Current platform time. */
    @RoboChartType("nat")
    public long nowMs() {
        return now;
    }

    /** Advances platform time by the given amount. */
    public void advance(@RoboChartType("nat") long elapsed) {
        this.now = this.now + elapsed;
    }
}
