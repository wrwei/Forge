package sranger.sensor;

import sranger.annotation.RoboChartType;
import sranger.annotation.SensorService;

/**
 * SR-DM5 / SR-SF1: Sensor exposes the latest IR distance reading.
 * Returns a large default value when no reading is available so the
 * obstacle-detection condition is not falsely triggered.
 */
@SensorService
public final class Sensor {

    @RoboChartType("real")
    private double latestDistance = 1.0e6;

    public void updateDistance(@RoboChartType("real") double metres) {
        this.latestDistance = metres;
    }

    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
