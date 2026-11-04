package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest forward-facing IR distance reading.
 */
public final class Sensor {

    // Returned while no reading is available, far above the obstacle threshold.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement. Negative or NaN readings are invalid and
     * are ignored, so distance() stays non-negative.
     *
     * @param reading measured distance in metres
     */
    public void update(@RoboChartType("real") double reading) {
        if (reading >= 0.0) {
            this.latestDistance = reading;
        }
    }

    /** Marks the reading as unavailable; distance() returns the large default again. */
    public void clearReading() {
        this.latestDistance = NO_READING_DISTANCE;
    }

    /**
     * Latest IR distance reading.
     *
     * @return distance in metres (non-negative); a large default when no reading is available
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
