package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest IR distance reading to the controller. Until the
 * first reading arrives, {@link #distance()} returns a large default so
 * the obstacle-detection condition is not falsely triggered by missing
 * data.
 */
public final class Sensor {

    @RoboChartType("real")
    private double distanceReading = 1000.0;

    /** Stores the latest IR distance measurement (metres, non-negative). */
    public void update(@RoboChartType("real") double value) {
        this.distanceReading = value;
    }

    /** Latest IR distance reading in metres (large default when no reading is available). */
    @RoboChartType("real")
    public double distance() {
        return distanceReading;
    }
}
