package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest forward-facing IR distance reading (SR-DM5, SR-SF1).
 */
public final class Sensor {

    // Returned while no valid reading is available; far beyond the IR range,
    // so the obstacle-detection condition is not triggered by missing data.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement. A negative or NaN measurement is not a
     * valid reading and leaves the sensor without one.
     */
    public void update(@RoboChartType("real") double reading) {
        if (reading >= 0.0) {
            this.latestDistance = reading;
        } else {
            this.latestDistance = NO_READING_DISTANCE;
        }
    }

    /** Discards the current reading, for example when the IR link is lost. */
    public void clearReading() {
        this.latestDistance = NO_READING_DISTANCE;
    }

    /** The latest IR distance in metres, non-negative; a large default when no reading is available. */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
