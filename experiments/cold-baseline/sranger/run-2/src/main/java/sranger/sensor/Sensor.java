package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * IR distance sensor. Exposes the latest IR distance reading.
 * Requirements: SR-DM5, SR-SF1.
 *
 * When no reading is available the sensor returns a large default value so
 * that the obstacle-detection condition is not falsely triggered by missing
 * data.
 */
public final class Sensor {

    /** Default distance returned when no IR reading is available. */
    @RoboChartType("real")
    public static final double DEFAULT_DISTANCE = 1000.0;

    @RoboChartType("real")
    private double currentDistance = DEFAULT_DISTANCE;

    /** Inject the latest IR reading (metres, non-negative). */
    public void update(@RoboChartType("real") double distanceMetres) {
        if (distanceMetres < 0.0) {
            this.currentDistance = DEFAULT_DISTANCE;
        } else {
            this.currentDistance = distanceMetres;
        }
    }

    /**
     * Returns the latest IR distance reading in metres.
     * Non-negative. When no reading is available, returns a large default
     * value so that the obstacle-detection condition is not falsely
     * triggered by missing data.
     */
    @RoboChartType("real")
    public double distance() {
        return currentDistance;
    }
}
