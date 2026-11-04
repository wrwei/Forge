package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/**
 * The Vehicle's odometer: the cumulative distance travelled.
 */
public final class OdometerSensor {

    @RoboChartType("real") private double distance = 0.0;

    /**
     * Records the cumulative distance the Vehicle reports.
     */
    public void update(@RoboChartType("real") double distance) {
        this.distance = distance;
    }

    /**
     * The cumulative distance travelled so far.
     */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }
}
