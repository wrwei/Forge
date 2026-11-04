package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest forward IR distance reading (SR-DM5, SR-SF1).
 */
public final class Sensor {

    // Reported while no IR measurement is available; large enough that the
    // obstacle-detection condition is never triggered by missing data.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement in metres. A negative or NaN reading is
     * not a valid distance and is treated as no reading available.
     */
    public void update(@RoboChartType("real") double reading) {
        if (reading >= 0.0) {
            this.latestDistance = reading;
        } else {
            this.latestDistance = NO_READING_DISTANCE;
        }
    }

    /** Latest IR distance reading, metres, non-negative. */
    @RoboChartType("real") public double distance() {
        return latestDistance;
    }
}
