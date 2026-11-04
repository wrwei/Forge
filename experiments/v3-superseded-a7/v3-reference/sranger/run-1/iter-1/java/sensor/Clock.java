package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Time source of the controller, in seconds since start-up. The system
 * framework advances it as time passes.
 */
public final class Clock {

    @RoboChartType("real") private double currentTime = 0.0;

    /**
     * Returns the current time.
     *
     * @return the current time in seconds
     */
    @RoboChartType("real")
    public double now() {
        return currentTime;
    }

    /**
     * Advances the time; time never runs backwards, so a non-positive amount is ignored.
     *
     * @param seconds the elapsed time in seconds
     */
    public void advance(@RoboChartType("real") double seconds) {
        if (seconds > 0.0) {
            this.currentTime = this.currentTime + seconds;
        }
    }
}
