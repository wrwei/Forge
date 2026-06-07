package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest IR distance reading to the controller. Returns a
 * large default when no reading is available so the obstacle-detection
 * condition is never falsely triggered by missing data.
 */
public final class Sensor {

    @RoboChartType("real")
    private static final double NO_READING_DEFAULT = 1000.0;

    @RoboChartType("real")
    private double irDistance = NO_READING_DEFAULT;

    /** Records the latest IR measurement (metres, non-negative). */
    public void update(@RoboChartType("real") double value) {
        this.irDistance = value;
    }

    /** Latest IR distance reading in metres. */
    @RoboChartType("real")
    public double distance() {
        return irDistance;
    }
}
