package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Sensor service exposing the latest forward IR distance reading.
 */
public final class Sensor {

    // Reported while no valid reading is available; well above the obstacle threshold.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestReading = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement. A negative or NaN measurement is not a valid
     * reading, so the sensor falls back to the large no-reading default.
     *
     * @param metres the measured distance in metres
     */
    public void update(@RoboChartType("real") double metres) {
        if (metres >= 0.0) {
            this.latestReading = metres;
        } else {
            this.latestReading = NO_READING_DISTANCE;
        }
    }

    /**
     * Returns the latest IR distance reading in metres (non-negative); a large
     * default when no reading is available.
     *
     * @return the distance in metres
     */
    @RoboChartType("real")
    public double distance() {
        return latestReading;
    }
}
