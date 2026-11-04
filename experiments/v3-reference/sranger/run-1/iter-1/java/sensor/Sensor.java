package sranger.sensor;

import sranger.annotation.RoboChartType;

/** The forward-facing IR distance sensor. */
public final class Sensor {

    // Returned while no reading is available: far beyond any obstacle threshold,
    // so missing data never looks like an obstacle.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records an IR distance reading in metres. A negative or NaN value means
     * no valid reading is available.
     */
    public void update(@RoboChartType("real") double metres) {
        if (metres >= 0.0) {
            this.latestDistance = metres;
        } else {
            this.latestDistance = NO_READING_DISTANCE;
        }
    }

    /**
     * The latest IR distance reading in metres (non-negative); a large default
     * when the sensor has no reading yet.
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
