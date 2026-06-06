package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * SR-DM5 / SR-SF1: exposes the latest IR distance reading to the controller.
 *
 * When no reading has been produced yet the sensor returns a large default
 * value so the obstacle-detection guard is not falsely triggered.
 */
public final class Sensor {

    /** Default returned when the IR has produced no measurement yet. */
    @RoboChartType("real")
    private static final double DEFAULT_DISTANCE = 1.0e6;

    @RoboChartType("real")
    private double latestDistance = DEFAULT_DISTANCE;

    private boolean hasReading = false;

    /** Update the latest IR distance reading (metres, non-negative). */
    public void update(@RoboChartType("real") double distance) {
        this.latestDistance = distance;
        this.hasReading = true;
    }

    /**
     * SR-SF1: latest IR distance reading in metres.
     *
     * Returns DEFAULT_DISTANCE when no reading has been produced yet, so
     * downstream guard predicates can call this directly without a sentinel
     * check.
     */
    @RoboChartType("real")
    public double distance() {
        if (hasReading) {
            return latestDistance;
        }
        return DEFAULT_DISTANCE;
    }
}
