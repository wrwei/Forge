package chemical_detector.timing;

import chemical_detector.annotation.RoboChartType;

/** Monotonic time source, in abstract time units. */
public final class Clock {

    @RoboChartType("nat")
    private long currentTime;

    /** Current time. */
    @RoboChartType("nat")
    public long now() {
        return currentTime;
    }

    /** Advances time by {@code duration} units. */
    public void advance(@RoboChartType("nat") long duration) {
        this.currentTime = currentTime + duration;
    }
}
