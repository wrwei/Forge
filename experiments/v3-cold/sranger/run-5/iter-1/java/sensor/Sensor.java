package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest IR distance reading to the controller (SR-DM5, SR-SF1).
 * Until a valid reading arrives, distance() returns a large default so that
 * the obstacle-detection condition is not triggered by missing data.
 */
public final class Sensor {

    // Large distance reported when no reading is available (m).
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement in metres. A negative or NaN value is not a
     * valid reading and resets the sensor to its no-reading default.
     */
    public void update(@RoboChartType("real") double reading) {
        if (reading >= 0.0) {
            this.latestDistance = reading;
        } else {
            this.latestDistance = NO_READING_DISTANCE;
        }
    }

    /** Latest IR distance reading in metres (non-negative). */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
