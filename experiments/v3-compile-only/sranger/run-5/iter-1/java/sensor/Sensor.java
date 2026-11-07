package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest forward IR distance reading (SR-DM5, SR-SF1).
 */
public final class Sensor {

    /** Reading reported while no valid measurement is available (metres). */
    @RoboChartType("real")
    private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real")
    private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement in metres. A negative or NaN measurement is
     * not a valid distance and is treated as "no reading available".
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
