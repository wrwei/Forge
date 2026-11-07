package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Sensor interface of the SRanger controller: the forward IR distance
 * reading (SR-DM5, SR-SF1).
 */
public final class Sensor {

    /**
     * Value returned when no reading is available (sensor not yet
     * initialised, or an invalid reading). Large enough that the
     * obstacle-detection condition is never triggered by missing data.
     */
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /** Latest IR distance reading in metres; non-negative. */
    @RoboChartType("real") public double distance() {
        return latestDistance;
    }

    /**
     * Records a new IR reading from the sensor framework. A negative or
     * NaN reading means no reading is available and resets the value to
     * the large default.
     */
    public void update(@RoboChartType("real") double reading) {
        if (reading >= 0.0) {
            latestDistance = reading;
        } else {
            latestDistance = NO_READING_DISTANCE;
        }
    }
}
