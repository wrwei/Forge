package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * The IR distance sensor (SR-DM5, SR-SF1). Holds the latest forward IR
 * reading in metres.
 */
public final class Sensor {

    // Returned when no valid reading is available, so that the
    // obstacle-detection condition is not triggered by missing data.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement. A negative or NaN measurement is not a
     * valid distance and is treated as "no reading available".
     *
     * @param metres the measured distance in metres
     */
    public void update(@RoboChartType("real") double metres) {
        if (metres >= 0.0) {
            this.latestDistance = metres;
        } else {
            this.latestDistance = NO_READING_DISTANCE;
        }
    }

    /**
     * Returns the latest IR distance reading in metres (non-negative), or a
     * large default value when no reading is available.
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
