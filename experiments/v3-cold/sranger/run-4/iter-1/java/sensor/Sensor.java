package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Sensor service exposing the latest IR distance reading.
 */
public final class Sensor {

    /** Distance reported when no valid reading is available; far above the obstacle threshold. */
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR distance measurement. A negative or NaN measurement is
     * not a valid reading and is treated as no reading available.
     *
     * @param reading measured distance in metres
     */
    public void update(@RoboChartType("real") double reading) {
        if (reading >= 0.0) {
            this.latestDistance = reading;
        } else {
            this.latestDistance = NO_READING_DISTANCE;
        }
    }

    /**
     * Returns the latest IR distance reading in metres (non-negative).
     *
     * @return the latest distance, or a large default when no reading is available
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
