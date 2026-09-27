package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.annotation.SensorService;

/** Cumulative distance travelled by the Vehicle, as last reported by the odometer. */
@SensorService
public final class OdometerSensor {

    @RoboChartType("real")
    private double distance;

    /** Records the latest odometer report. */
    public void update(@RoboChartType("real") double travelled) {
        this.distance = travelled;
    }

    /** Current cumulative distance travelled; {@code 0.0} before the first report. */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }
}
