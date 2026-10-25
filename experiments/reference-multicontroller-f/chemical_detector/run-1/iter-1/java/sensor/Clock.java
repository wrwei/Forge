package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** Logical time source shared by the controllers, advanced by the platform. */
public final class Clock {

    @RoboChartType("nat")
    private long now;

    /** Current time in controller time units. */
    @RoboChartType("nat")
    public long nowMs() {
        return now;
    }

    /** Advances time by the given number of units. */
    public void advance(@RoboChartType("nat") long elapsed) {
        now = now + elapsed;
    }
}
