package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Forward-facing IR distance sensor.
 */
public final class Sensor {

    // Distance reported while no valid reading is available, in metres. Far above the
    // obstacle-detection threshold, so missing data never looks like an obstacle.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 100.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement, in metres. A negative or non-numeric measurement is not a
     * valid distance and is treated as no reading.
     */
    public void update(@RoboChartType("real") double metres) {
        if (metres >= 0.0) {
            this.latestDistance = metres;
        } else {
            this.latestDistance = NO_READING_DISTANCE;
        }
    }

    /** The latest IR distance reading in metres; never negative. */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
