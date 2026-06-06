package sranger.clock;

import sranger.annotation.RoboChartType;

/**
 * Clock dependency used by the controller to record entry times of timed states.
 * The name "Clock" matches the M2M convention for clock-typed dependencies.
 */
public final class Clock {

    @RoboChartType("real")
    private double nowSeconds = 0.0;

    public void advance(@RoboChartType("real") double deltaSeconds) {
        this.nowSeconds = this.nowSeconds + deltaSeconds;
    }

    public void setNow(@RoboChartType("real") double nowSeconds) {
        this.nowSeconds = nowSeconds;
    }

    @RoboChartType("real")
    public double nowSeconds() {
        return nowSeconds;
    }

    @RoboChartType("real")
    public double nowMs() {
        return nowSeconds * 1000.0;
    }
}
