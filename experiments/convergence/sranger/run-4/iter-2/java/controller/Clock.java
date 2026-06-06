package sranger.controller;

import sranger.annotation.RoboChartType;

/**
 * System clock dependency of the SRanger controller. The class name
 * "Clock" is the M2M convention for RoboChart clock extraction: fields
 * assigned from a method call on a Clock-typed receiver are promoted to
 * RoboChart clocks.
 */
public final class Clock {

    @RoboChartType("real")
    private double nowSeconds;

    /** Advances the clock by dt seconds (driven by the test/simulation harness). */
    public void advance(@RoboChartType("real") double dt) {
        this.nowSeconds = this.nowSeconds + dt;
    }

    /** Current time in seconds since start-up. */
    public double now() {
        return nowSeconds;
    }
}
