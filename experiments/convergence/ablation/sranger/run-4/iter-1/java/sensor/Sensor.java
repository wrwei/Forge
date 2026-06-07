package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest IR distance reading to the controller. When no
 * reading is available yet, {@link #distance()} returns a large default
 * value so that the obstacle-detection condition is not falsely
 * triggered by missing data.
 */
public final class Sensor {

    private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real")
    private double distance = NO_READING_DISTANCE;

    /** Records the latest IR distance measurement (metres, non-negative). */
    public void setDistance(@RoboChartType("real") double value) {
        this.distance = value;
    }

    /** Latest IR distance reading in metres. */
    public double distance() {
        return distance;
    }
}
