package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest IR distance reading to the controller (SR-DM5, SR-SF1).
 * When no reading is available the sensor returns a large default so the
 * obstacle-detection condition is not falsely triggered by missing data.
 */
public final class Sensor {

    @RoboChartType("real")
    private static final double DEFAULT_DISTANCE = 1000.0;

    @RoboChartType("real")
    private double distance = DEFAULT_DISTANCE;

    /** Update the latest IR distance reading (metres, non-negative). */
    public void update(@RoboChartType("real") double reading) {
        this.distance = reading;
    }

    /** The latest IR distance reading in metres (non-negative). */
    @RoboChartType("real")
    public double distance() {
        return distance;
    }
}
