package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest forward IR distance reading (SR-DM5, SR-SF1).
 */
public final class Sensor {

    /** Distance reported while no valid IR reading is available (m). */
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 100.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR distance reading in metres. A reading that is not a
     * non-negative number counts as no reading available.
     */
    public void update(@RoboChartType("real") double reading) {
        if (reading >= 0.0) {
            this.latestDistance = reading;
        } else {
            this.latestDistance = NO_READING_DISTANCE;
        }
    }

    /**
     * Latest IR distance reading in metres, non-negative; a large default
     * while no reading is available, so that obstacle detection is not
     * falsely triggered.
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
