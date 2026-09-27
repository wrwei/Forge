package sranger.clock;

import sranger.annotation.RoboChartType;

/**
 * Monotonic time source for the controller's timed Turning to Moving
 * transition (SR-Var1). Readings are in seconds.
 *
 * <p>The class name is the convention the M2M uses to promote controller
 * fields assigned from it into RoboChart clocks.
 */
public final class Clock {

    @RoboChartType("real")
    public double now() {
        return System.nanoTime() / 1000000000.0;
    }
}
