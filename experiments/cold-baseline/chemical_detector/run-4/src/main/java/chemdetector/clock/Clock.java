package chemdetector.clock;

import chemdetector.annotation.RoboChartType;

/**
 * Monotonic millisecond clock used to back the movement subsystem's T clock for
 * stuck detection (CD-MV-Clock1).
 */
public final class Clock {

    @RoboChartType("nat")
    public long nowMs() {
        return System.currentTimeMillis();
    }
}
