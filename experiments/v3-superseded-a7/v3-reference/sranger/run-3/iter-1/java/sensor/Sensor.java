package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * The forward-facing IR distance sensor (SR-DM5, SR-SF1).
 */
public final class Sensor {

    // Returned while no valid reading is available, far above the obstacle
    // threshold so that missing data never looks like an obstacle.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement in metres. A negative or NaN measurement is
     * not a valid reading and resets the sensor to its no-reading value.
     */
    public void update(@RoboChartType("real") double reading) {
        if (reading >= 0.0) {
            this.latestDistance = reading;
        } else {
            this.latestDistance = NO_READING_DISTANCE;
        }
    }

    /**
     * The latest IR distance reading in metres, non-negative; a large default
     * value when no reading is available.
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
