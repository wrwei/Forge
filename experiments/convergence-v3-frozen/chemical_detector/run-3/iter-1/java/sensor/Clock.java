package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/**
 * Logical time source, advanced once per control cycle (one time unit per tock).
 */
public final class Clock {

    @RoboChartType("nat")
    private long time;

    /** Current time in time units. */
    @RoboChartType("nat")
    public long now() {
        return this.time;
    }

    /** Lets {@code units} time units pass. */
    public void advance(@RoboChartType("nat") long units) {
        this.time = this.time + units;
    }
}
