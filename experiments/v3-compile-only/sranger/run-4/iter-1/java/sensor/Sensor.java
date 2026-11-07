package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Sensor service exposing the latest forward IR distance reading (SR-DM5, SR-SF1).
 */
public final class Sensor {

    /**
     * Value reported when no reading is available (not yet initialised, or the
     * last reading was invalid). Far above any obstacle threshold, so missing
     * data never triggers the obstacle-detection condition.
     */
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 100.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement in metres. A negative or NaN measurement is
     * not a valid distance and is treated as "no reading available".
     *
     * @param reading the measured distance in metres
     */
    public void update(@RoboChartType("real") double reading) {
        if (reading >= 0.0) {
            this.latestDistance = reading;
        } else {
            this.latestDistance = NO_READING_DISTANCE;
        }
    }

    /**
     * Latest IR distance reading in metres (non-negative); a large default when
     * no reading is available.
     *
     * @return the distance in metres
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
