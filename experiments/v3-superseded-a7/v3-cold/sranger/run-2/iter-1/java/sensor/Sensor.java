package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest forward IR distance reading (SR-DM5, SR-SF1).
 */
public final class Sensor {

    // Returned while no reading is available: far above the obstacle
    // threshold, so missing data never looks like an obstacle.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 100.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR distance reading in metres. A negative or NaN reading
     * is not a valid distance and is ignored.
     */
    public void update(@RoboChartType("real") double reading) {
        if (reading >= 0.0) {
            this.latestDistance = reading;
        }
    }

    /** The latest IR distance reading in metres, never negative. */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
