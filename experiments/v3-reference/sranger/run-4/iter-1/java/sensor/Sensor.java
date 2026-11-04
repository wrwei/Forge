package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest forward-facing IR distance reading.
 */
public final class Sensor {

    // Reported while no valid reading is available: far beyond the obstacle
    // threshold, so missing data never triggers obstacle detection.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement (m). A negative or NaN measurement is not a
     * valid reading and is treated as no reading available.
     */
    public void update(@RoboChartType("real") double reading) {
        if (Double.isNaN(reading) || reading < 0.0) {
            this.latestDistance = NO_READING_DISTANCE;
        } else {
            this.latestDistance = reading;
        }
    }

    /**
     * The latest IR distance reading (m, non-negative); a large default value
     * when no reading is available.
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
