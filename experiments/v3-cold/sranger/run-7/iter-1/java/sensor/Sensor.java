package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest forward-facing IR distance reading.
 */
public final class Sensor {

    // Returned while no valid reading is available; far above the obstacle threshold.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement. A negative or NaN measurement is treated as
     * no reading, so it cannot trigger obstacle detection.
     *
     * @param reading measured distance (m)
     */
    public void update(@RoboChartType("real") double reading) {
        if (Double.isNaN(reading) || reading < 0.0) {
            latestDistance = NO_READING_DISTANCE;
        } else {
            latestDistance = reading;
        }
    }

    /**
     * Returns the latest IR distance reading, or a large default when no
     * reading is available.
     *
     * @return distance (m), non-negative
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
