package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest reading of the forward-facing IR distance sensor.
 */
public final class Sensor {

    /** Returned when no reading is available, far above any obstacle threshold. */
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 100.0;

    @RoboChartType("real") private double latestReading = NO_READING_DISTANCE;
    private boolean readingAvailable = false;

    /**
     * Records a new IR measurement. A negative or NaN measurement is not a
     * valid distance and leaves the sensor without a reading.
     *
     * @param metres the measured distance, metres
     */
    public void update(@RoboChartType("real") double metres) {
        if (metres >= 0.0) {
            this.latestReading = metres;
            this.readingAvailable = true;
        } else {
            this.latestReading = NO_READING_DISTANCE;
            this.readingAvailable = false;
        }
    }

    /**
     * The latest IR distance reading in metres (non-negative), or a large
     * default when no reading is available.
     */
    @RoboChartType("real")
    public double distance() {
        if (readingAvailable) {
            return latestReading;
        }
        return NO_READING_DISTANCE;
    }
}
