package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest forward-facing IR distance reading (SR-DM5, SR-SF1).
 */
public final class Sensor {

    // Returned while no valid reading is available, so that the
    // obstacle-detection condition is not triggered by missing data.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement. A negative or NaN measurement counts as
     * "no reading available".
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
     * @return the latest IR distance reading in metres (non-negative), or a
     *         large default value when no reading is available
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
