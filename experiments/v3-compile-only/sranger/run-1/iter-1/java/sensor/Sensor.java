package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest forward-facing IR distance reading (SR-DM5, SR-SF1).
 * When no reading is available the sensor reports a large default distance,
 * so that obstacle detection is not falsely triggered by missing data.
 */
public final class Sensor {

    /** Distance reported when no reading is available (metres). */
    @RoboChartType("real")
    private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real")
    private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement. A negative value is not a valid distance
     * and is treated as "no reading available".
     *
     * @param reading the measured distance in metres
     */
    public void update(@RoboChartType("real") double reading) {
        if (reading < 0.0) {
            this.latestDistance = NO_READING_DISTANCE;
        } else {
            this.latestDistance = reading;
        }
    }

    /** Marks the current reading as unavailable. */
    public void clearReading() {
        this.latestDistance = NO_READING_DISTANCE;
    }

    /**
     * Returns the latest IR distance reading in metres (non-negative).
     *
     * @return the latest reading, or a large default when none is available
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
