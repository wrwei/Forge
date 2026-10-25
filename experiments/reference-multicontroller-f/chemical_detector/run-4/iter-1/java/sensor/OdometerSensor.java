package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/**
 * Latest cumulative distance reported by the Vehicle's odometer (0 before the first report).
 */
public final class OdometerSensor {

    @RoboChartType("real")
    private double distance;

    public void update(@RoboChartType("real") double distance) {
        this.distance = distance;
    }

    @RoboChartType("real")
    public double odometer() {
        return distance;
    }
}
