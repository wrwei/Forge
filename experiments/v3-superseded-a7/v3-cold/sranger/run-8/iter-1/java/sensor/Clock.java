package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Monotonic time source for the controller's timed transition (SR-Var1).
 */
public final class Clock {

    @RoboChartType("real") private final double startSeconds;

    /** Creates a clock whose time starts at zero now. */
    public Clock() {
        this.startSeconds = System.nanoTime() / 1000000000.0;
    }

    /** Returns the seconds elapsed since this clock was created (non-negative). */
    @RoboChartType("real")
    public double now() {
        return System.nanoTime() / 1000000000.0 - startSeconds;
    }
}
