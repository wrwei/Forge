package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;

/**
 * Provides monotonic time. The movement controller stores the value
 * of nowMs() at clock-reset points, then computes elapsed time as
 * the difference between the current nowMs() and the stored value.
 *
 * In the RoboChart model this maps to a clock variable (CD-MV-Clock1)
 * and the difference is rewritten into since(T).
 */
public final class Clock {

    private long startMs = System.currentTimeMillis();

    @RoboChartType("nat")
    public long nowMs() {
        return System.currentTimeMillis() - startMs;
    }
}
