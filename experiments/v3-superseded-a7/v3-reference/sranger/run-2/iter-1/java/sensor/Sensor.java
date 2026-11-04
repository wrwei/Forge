package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * The forward-facing IR distance sensor (SR-DM5, SR-SF1).
 */
public final class Sensor {

    /** Reported while no valid reading is available; far above any obstacle threshold. */
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement. A negative value is not a valid distance
     * and is treated as no reading available.
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
     * The latest IR distance reading in metres (non-negative), or a large
     * default value when no reading is available.
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
