package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest IR distance reading (SR-DM5, SR-SF1).
 */
public final class Sensor {

    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement. A negative or non-numeric reading is not a
     * valid distance and is treated as no reading.
     *
     * @param reading the measured distance, metres
     */
    public void update(@RoboChartType("real") double reading) {
        if (reading >= 0.0) {
            this.latestDistance = reading;
        } else {
            this.latestDistance = NO_READING_DISTANCE;
        }
    }

    /**
     * Returns the latest IR distance reading in metres, never negative. Before
     * the first valid reading it returns a large default value, so that the
     * obstacle-detection condition is not triggered by missing data.
     *
     * @return the latest IR distance, metres
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
