package sranger.sensor;

import sranger.annotation.RoboChartType;
import sranger.annotation.SensorService;

/**
 * Sensor for the SRanger controller. Exposes the latest IR distance
 * reading. Returns a large default value when no reading is available
 * so that the obstacle-detection condition is not falsely triggered
 * by missing data.
 */
@SensorService
public final class Sensor {

    /** Large default returned when no IR reading has been recorded. */
    @RoboChartType("real")
    private static final double DEFAULT_DISTANCE = 1.0e6;

    @RoboChartType("real")
    private double latestDistance = DEFAULT_DISTANCE;

    private boolean initialised = false;

    /** Update the latest IR distance reading (metres, non-negative). */
    public void update(@RoboChartType("real") double distance) {
        this.latestDistance = distance;
        this.initialised = true;
    }

    /** Returns the latest IR distance reading in metres. */
    @RoboChartType("real")
    public double distance() {
        if (!initialised) {
            return DEFAULT_DISTANCE;
        }
        return latestDistance;
    }
}
