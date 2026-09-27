package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** Platform wall clock. Assignments from {@link #nowMs()} become RoboChart clocks. */
public final class Clock {

    @RoboChartType("real")
    private double elapsed = 0.0;

    /** Current time, in the platform's time unit. */
    @RoboChartType("real")
    public double nowMs() {
        return elapsed;
    }

    /** Advance the clock; called by the platform harness, never by a controller. */
    public void advance(@RoboChartType("real") double delta) {
        this.elapsed = this.elapsed + delta;
    }
}
