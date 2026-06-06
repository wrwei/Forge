package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest IR distance reading to the controller (SR-DM5, SR-SF1).
 * When no reading is available the sensor returns a large default value so the
 * obstacle-detection condition is not falsely triggered by missing data.
 */
public final class Sensor {

    /** Large default returned before any real reading is available (metres). */
    @RoboChartType("real")
    public static final double NO_READING_DEFAULT = 1000.0;

    @RoboChartType("real")
    private double distance = NO_READING_DEFAULT;

    /** Update the latest IR distance reading (metres, non-negative). */
    public void update(@RoboChartType("real") double metres) {
        this.distance = metres;
    }

    /** The latest IR distance reading in metres (non-negative). */
    @RoboChartType("real")
    public double distance() {
        return distance;
    }
}
