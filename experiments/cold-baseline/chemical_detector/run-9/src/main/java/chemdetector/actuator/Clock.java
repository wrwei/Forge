package chemdetector.actuator;

import chemdetector.annotation.RoboChartType;

/**
 * System clock for stuck detection. Exposes the current time in milliseconds and
 * is referenced by the movement controller's stuck-detection logic. The class is
 * named {@code Clock} so the ETL recognises the dependency by convention.
 */
public final class Clock {

    @RoboChartType("nat")
    private long now = 0L;

    @RoboChartType("nat")
    public long nowMs() {
        return now;
    }

    public void advanceMs(long delta) {
        this.now = this.now + delta;
    }
}
