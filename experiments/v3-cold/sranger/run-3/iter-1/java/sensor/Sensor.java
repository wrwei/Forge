package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * The forward-facing IR distance sensor, as the controller sees it.
 */
public final class Sensor {

    // Returned while no reading is available; far above the obstacle threshold
    // so that missing data never looks like an obstacle.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement. A negative or non-numeric value is treated
     * as "no reading available".
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
     * Returns the latest IR distance reading in metres (never negative), or a
     * large default value when no reading is available.
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
