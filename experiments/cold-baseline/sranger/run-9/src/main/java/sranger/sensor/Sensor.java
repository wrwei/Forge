package sranger.sensor;

import sranger.annotation.RoboChartType;
import sranger.annotation.SensorService;

/**
 * SRanger sensor service exposing the IR distance reading (SR-DM5, SR-SF1).
 * Returns a large default when no reading is available so that the
 * obstacle-detection condition is not falsely triggered by missing data.
 */
@SensorService
public final class Sensor {

    @RoboChartType("real")
    private static final double DEFAULT_DISTANCE = 1.0e6;

    @RoboChartType("real")
    private double latestDistance = DEFAULT_DISTANCE;

    private boolean hasReading = false;

    public void updateDistance(@RoboChartType("real") double distance) {
        if (distance < 0.0) {
            this.latestDistance = DEFAULT_DISTANCE;
            this.hasReading = false;
        } else {
            this.latestDistance = distance;
            this.hasReading = true;
        }
    }

    public void clearReading() {
        this.latestDistance = DEFAULT_DISTANCE;
        this.hasReading = false;
    }

    @RoboChartType("real")
    public double distance() {
        if (hasReading) {
            return latestDistance;
        }
        return DEFAULT_DISTANCE;
    }
}
