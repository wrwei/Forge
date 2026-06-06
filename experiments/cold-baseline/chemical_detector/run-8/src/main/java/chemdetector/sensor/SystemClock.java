package chemdetector.sensor;

import chemdetector.annotation.Clock;
import chemdetector.annotation.RoboChartType;

/**
 * Monotonic millisecond clock used by the movement subsystem for
 * stuck detection. nowMs() returns the elapsed time since process
 * start; controllers store this value into a clock-typed field on
 * reset and compute (nowMs() - field) when evaluating since(T).
 */
@Clock
public final class SystemClock {

    @RoboChartType("nat")
    public int nowMs() {
        long ms = System.nanoTime() / 1_000_000L;
        if (ms < 0L) {
            return 0;
        }
        if (ms > (long) Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) ms;
    }
}
