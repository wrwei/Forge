package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * The forward-facing IR distance sensor.
 */
public final class Sensor {

    // Reported while no reading is available: far beyond the obstacle threshold,
    // so missing data never looks like an obstacle.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement, in metres. A negative or NaN value is not a
     * valid reading and counts as no reading.
     */
    public void update(@RoboChartType("real") double metres) {
        if (metres >= 0.0) {
            this.latestDistance = metres;
        } else {
            this.latestDistance = NO_READING_DISTANCE;
        }
    }

    /** Marks the IR reading as unavailable. */
    public void clearReading() {
        this.latestDistance = NO_READING_DISTANCE;
    }

    /** The latest IR distance reading, in metres; never negative. */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
