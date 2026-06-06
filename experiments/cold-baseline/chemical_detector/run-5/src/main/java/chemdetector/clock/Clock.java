package chemdetector.clock;

import chemdetector.annotation.RoboChartType;

/**
 * System clock dependency used by the movement controller for stuck
 * detection (CD-MV-Clock1). nowMs returns the current monotonic time
 * in milliseconds.
 */
public final class Clock {

    @RoboChartType("nat")
    public int nowMs() {
        return (int) (System.currentTimeMillis() & 0x7fffffff);
    }
}
