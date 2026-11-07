package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Sensor interface exposing the latest IR distance reading (SR-DM5, SR-SF1).
 *
 * <p>Until a valid reading has been recorded, {@link #distance()} returns a
 * large default so that the obstacle-detection condition is not triggered by
 * missing data.
 */
public final class Sensor {

    /** Distance reported when no valid reading is available (m). */
    private static final double NO_READING_DISTANCE = 100.0;

    @RoboChartType("real")
    private double latestDistance = NO_READING_DISTANCE;

    /**
     * Latest IR distance reading.
     *
     * @return the distance in metres, non-negative
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }

    /**
     * Records a new IR measurement. A negative or non-numeric measurement is
     * not a valid reading and is stored as "no reading available".
     *
     * @param reading the measured distance in metres
     */
    public void recordReading(@RoboChartType("real") double reading) {
        if (reading >= 0.0) {
            this.latestDistance = reading;
        } else {
            this.latestDistance = NO_READING_DISTANCE;
        }
    }
}
