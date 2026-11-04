package sranger.controller;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, in seconds (SR-Var1, SR-GP2).
 */
public final class Clock {

    @RoboChartType("real") private double currentTime = 0.0;

    /** Advances the clock to the given time; time never runs backwards. */
    public void update(@RoboChartType("real") double time) {
        if (time >= currentTime) {
            this.currentTime = time;
        }
    }

    /** Current time, seconds. */
    @RoboChartType("real") public double now() {
        return currentTime;
    }
}
