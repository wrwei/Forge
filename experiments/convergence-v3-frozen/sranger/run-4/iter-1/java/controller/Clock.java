package sranger.controller;

import sranger.annotation.RoboChartType;

/** Monotonic controller clock backing the timed Turning to Moving transition (SR-Var1). */
public final class Clock {

    @RoboChartType("real")
    private double currentMs;

    /** Advances the clock by the given number of milliseconds. */
    public void advance(@RoboChartType("real") double deltaMs) {
        this.currentMs = this.currentMs + deltaMs;
    }

    /** Current controller time in milliseconds. */
    @RoboChartType("real")
    public double nowMs() {
        return currentMs;
    }
}
