package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Sensor service exposing the latest forward-facing IR distance reading.
 */
public final class Sensor {

    // Reported while no reading is available, so that missing data never looks like an obstacle.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 100.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement in metres. A negative or NaN measurement is not a
     * valid distance and is treated as no reading.
     */
    public void update(@RoboChartType("real") double reading) {
        if (reading >= 0.0) {
            latestDistance = reading;
        } else {
            latestDistance = NO_READING_DISTANCE;
        }
    }

    /** Records that no IR reading is currently available. */
    public void markNoReading() {
        latestDistance = NO_READING_DISTANCE;
    }

    /** The latest IR distance reading in metres (non-negative). */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
