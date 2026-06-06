package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Sensor interface for the SRanger controller (SR-DM5, SR-SF1).
 * Exposes the latest IR distance reading. When no reading is
 * available, distance() returns a large default so that the
 * obstacle-detection condition is not falsely triggered.
 */
public final class Sensor {

    @RoboChartType("real")
    private static final double DEFAULT_DISTANCE = 1.0e6;

    @RoboChartType("real")
    private double latestDistance = DEFAULT_DISTANCE;

    public void update(@RoboChartType("real") double distance) {
        this.latestDistance = distance;
    }

    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
