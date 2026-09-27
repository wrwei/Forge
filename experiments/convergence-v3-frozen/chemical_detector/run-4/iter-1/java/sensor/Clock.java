package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** Monotonic time source in abstract clock units, advanced by the platform. */
public final class Clock {

    @RoboChartType("nat")
    private long currentTime;

    public void advance(@RoboChartType("nat") long units) {
        this.currentTime = this.currentTime + units;
    }

    @RoboChartType("nat")
    public long now() {
        return currentTime;
    }
}
