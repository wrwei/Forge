package sranger.controller;

import sranger.annotation.RoboChartType;

/** Monotonic time source for the controller's timed transitions. */
public final class Clock {

    private final long originNanos = System.nanoTime();

    /** Seconds elapsed since this clock was created. */
    @RoboChartType("real")
    public double now() {
        return (System.nanoTime() - originNanos) / 1000000000.0;
    }
}
