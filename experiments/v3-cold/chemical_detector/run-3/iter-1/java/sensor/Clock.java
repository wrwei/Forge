package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** Logical time in ticks, advanced by the platform and by delays. */
public final class Clock {

    @RoboChartType("nat") private long time = 0;

    /** Current time in ticks. */
    @RoboChartType("nat")
    public long now() {
        return time;
    }

    /** Advances time by {@code ticks}; called by the platform. */
    public void advance(@RoboChartType("nat") long ticks) {
        this.time = time + ticks;
    }

    /** Waits for {@code duration} ticks before the controller continues. */
    public void delay(@RoboChartType("nat") long duration) {
        this.time = time + duration;
    }
}
