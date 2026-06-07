package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest IR distance reading to the controller. Returns a large
 * default value while no reading is available, so the obstacle-detection
 * condition is not falsely triggered by missing data.
 */
public final class Sensor {

    private static final double NO_READING_DEFAULT = 100.0;

    @RoboChartType("real")
    private double distance = NO_READING_DEFAULT;

    /** Records the latest IR distance measurement (metres, non-negative). */
    public void update(@RoboChartType("real") double value) {
        this.distance = value;
    }

    /** Latest IR distance reading in metres. */
    @RoboChartType("real")
    public double distance() {
        return distance;
    }
}
