package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Forward-facing IR distance sensor (SR-DM5, SR-SF1).
 */
public final class Sensor {

    // Returned while no valid reading is available: large enough never to look like an obstacle.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestReading = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement. A negative or NaN reading is treated as no reading available.
     *
     * @param reading measured distance in metres
     */
    public void update(@RoboChartType("real") double reading) {
        if (reading >= 0.0) {
            this.latestReading = reading;
        } else {
            this.latestReading = NO_READING_DISTANCE;
        }
    }

    /**
     * Latest IR distance reading in metres, non-negative; a large default when no reading is available.
     */
    @RoboChartType("real")
    public double distance() {
        return latestReading;
    }
}
