package sranger.sensor;

import sranger.annotation.RoboChartType;
import sranger.annotation.SensorService;

/**
 * IR distance sensor (SR-DM5, SR-SF1). Exposes the latest IR distance reading
 * in metres. Returns a large default value when no reading is available so
 * the obstacle-detection condition is not falsely triggered by missing data.
 */
@SensorService
public final class Sensor {

    @RoboChartType("real")
    private static final double DEFAULT_LARGE_DISTANCE = 1.0e6;

    @RoboChartType("real")
    private double currentDistance = DEFAULT_LARGE_DISTANCE;

    private boolean initialised = false;

    /**
     * Update the sensor with a new IR reading in metres. Negative values
     * are clamped to zero (the sensor must return a non-negative number).
     */
    public void update(@RoboChartType("real") double metres) {
        if (metres < 0.0) {
            this.currentDistance = 0.0;
        } else {
            this.currentDistance = metres;
        }
        this.initialised = true;
    }

    /**
     * Returns the latest IR distance reading in metres (SR-SF1).
     * When no reading is available, returns a large default so guard
     * predicates that test against obstacleThreshold do not fire spuriously.
     */
    @RoboChartType("real")
    public double distance() {
        if (!initialised) {
            return DEFAULT_LARGE_DISTANCE;
        }
        return currentDistance;
    }
}
