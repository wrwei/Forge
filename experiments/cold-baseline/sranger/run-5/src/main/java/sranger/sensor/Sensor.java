package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * IR distance sensor for SRanger.
 * distance() returns the latest IR distance reading in metres (non-negative).
 * When no reading is available, returns a large default value so that the
 * obstacle-detection condition is not falsely triggered by missing data.
 */
public final class Sensor {

    @RoboChartType("real")
    private double latestDistance = Double.MAX_VALUE;

    private boolean hasReading = false;

    /** Update the latest IR distance measurement (called by the framework). */
    public void update(@RoboChartType("real") double value) {
        this.latestDistance = value;
        this.hasReading = true;
    }

    /**
     * The latest IR distance reading in metres.
     * Returns a large default value if no reading is available.
     */
    @RoboChartType("real")
    public double distance() {
        if (hasReading) {
            return latestDistance;
        }
        return Double.MAX_VALUE;
    }
}
