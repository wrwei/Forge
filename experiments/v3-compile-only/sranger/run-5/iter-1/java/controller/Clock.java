package sranger.controller;

import sranger.annotation.RoboChartType;

/**
 * Monotonic time source for the controller's clock-reset variable (SR-Var1).
 */
public final class Clock {

    private final long originNanos;

    /** Creates a clock whose time 0.0 is the moment of construction. */
    public Clock() {
        this.originNanos = System.nanoTime();
    }

    /** Current time in seconds since the clock was created. */
    @RoboChartType("real")
    public double now() {
        return (System.nanoTime() - originNanos) / 1.0e9;
    }
}
