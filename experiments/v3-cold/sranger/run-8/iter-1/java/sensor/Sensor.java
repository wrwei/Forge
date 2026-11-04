package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest forward-facing IR distance reading.
 *
 * <p>Until a valid reading has been received, {@link #distance()} returns a large
 * default so that missing data never looks like an obstacle.
 */
public final class Sensor {

    /** Reading reported while no IR measurement is available, metres. */
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 100.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Updates the latest IR reading. A negative or NaN measurement is not a valid
     * distance and is ignored; the previous reading (or the default) is kept.
     *
     * @param metres measured distance, metres
     */
    public void updateDistance(@RoboChartType("real") double metres) {
        if (metres >= 0.0) {
            this.latestDistance = metres;
        }
    }

    /**
     * Returns the latest IR distance reading in metres, never negative.
     *
     * @return distance, metres
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
