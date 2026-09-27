package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest forward-facing IR distance reading (SR-DM5, SR-SF1).
 */
public final class Sensor {

    @RoboChartType("real")
    private static final double NO_READING_DISTANCE = Double.MAX_VALUE;

    @RoboChartType("real")
    private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement in metres. Negative readings are clamped to zero.
     */
    public void update(@RoboChartType("real") double reading) {
        if (reading < 0.0) {
            this.latestDistance = 0.0;
        } else {
            this.latestDistance = reading;
        }
    }

    /**
     * Discards the current reading, as when the IR sensor is not yet initialised.
     */
    public void clear() {
        this.latestDistance = NO_READING_DISTANCE;
    }

    /**
     * Latest IR distance reading in metres; a large default when no reading is available.
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
