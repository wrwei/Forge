package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Sensor interface of the SRanger controller: the latest forward IR distance reading
 * (SR-DM5, SR-SF1).
 */
public final class Sensor {

    // Reported while no reading is available, so that missing data never looks like an obstacle.
    @RoboChartType("real") private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real") private double latestDistance = NO_READING_DISTANCE;

    /** Records a new IR reading in metres; a negative or NaN reading is ignored. */
    public void update(@RoboChartType("real") double reading) {
        if (reading >= 0.0) {
            this.latestDistance = reading;
        }
    }

    /** The latest IR distance reading in metres, or a large default if none is available. */
    @RoboChartType("real") public double distance() { return latestDistance; }
}
