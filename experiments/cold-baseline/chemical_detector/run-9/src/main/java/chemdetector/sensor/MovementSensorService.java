package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.SensorService;

/**
 * Sensor service exposing the most recent odometer reading and the stuck-detection
 * derived quantities used by the movement controller.
 */
@SensorService
public final class MovementSensorService {

    @RoboChartType("real")
    private double currentDistance = 0.0;

    public void updateOdometer(@RoboChartType("real") double distance) {
        this.currentDistance = distance;
    }

    @RoboChartType("real")
    public double currentOdometer() {
        return currentDistance;
    }
}
