package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Forward-facing IR distance sensor of the SRanger robot.
 */
public final class Sensor {

    // Reported while no valid reading is available: far above the obstacle
    // threshold, so missing data never looks like an obstacle.
    @RoboChartType("real")
    private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real")
    private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement, in metres. A negative, non-numeric or
     * out-of-range measurement is treated as no reading.
     */
    public void update(@RoboChartType("real") double measuredDistance) {
        if (measuredDistance >= 0.0 && measuredDistance <= NO_READING_DISTANCE) {
            this.latestDistance = measuredDistance;
        } else {
            this.latestDistance = NO_READING_DISTANCE;
        }
    }

    /** Marks the IR reading as unavailable. */
    public void clearReading() {
        this.latestDistance = NO_READING_DISTANCE;
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
