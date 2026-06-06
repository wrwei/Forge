package chemdetector.clock;

import chemdetector.annotation.Clock;

/**
 * Wall-clock dependency used by the movement controller for stuck-detection.
 * The {@link #nowMs()} method returns a monotonically increasing time stamp.
 * The ETL recognises this class via the {@link Clock} annotation regardless
 * of its name.
 */
@Clock
public final class SystemClock {

    @chemdetector.annotation.RoboChartType("nat")
    public int nowMs() {
        return (int) (System.currentTimeMillis() & 0x7fffffff);
    }
}
