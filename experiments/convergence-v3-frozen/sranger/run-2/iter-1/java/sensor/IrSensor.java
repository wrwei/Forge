package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Forward-facing infrared distance sensor (SR-DM5, SR-SF1).
 *
 * <p>Before the first reading arrives the sensor reports a large default
 * distance, so the obstacle-detection condition is never triggered by
 * missing data and the controller needs no sentinel checks.
 */
public final class IrSensor {

    @RoboChartType("real")
    private double latestDistance = 1000.0;

    public void update(@RoboChartType("real") double metres) {
        this.latestDistance = metres;
    }

    /** Latest IR distance reading, in metres, non-negative. */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
