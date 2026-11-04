package sranger.time;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, in seconds, advanced by the system
 * framework (SR-Var1, SR-GP2).
 */
public final class Clock {

    @RoboChartType("real") private double currentTime = 0.0;

    /** Advances the clock to the given time; time never runs backwards. */
    public void update(@RoboChartType("real") double seconds) {
        if (seconds > currentTime) {
            this.currentTime = seconds;
        }
    }

    /** The current time in seconds. */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }
}
