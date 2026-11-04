package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Sensor service exposing the latest forward IR distance reading.
 */
public final class Sensor {
    // Reported while no valid reading is available, so that missing data never
    // satisfies the obstacle-detection condition. Metres.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR distance reading, in metres. A negative or NaN reading is
     * not a valid distance and is ignored; the previous reading is kept.
     */
    public void updateDistance(@RoboChartType("real") double reading) {
        if (reading >= 0.0) {
            latestDistance = reading;
        }
    }

    /**
     * Returns the latest IR distance reading in metres (non-negative), or a large
     * default value if no valid reading has been received yet.
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
