package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/**
 * The odometer: reports the cumulative distance the Vehicle has travelled.
 */
public final class OdometerSensor {

    @RoboChartType("real")
    private double distance = 0.0;

    /**
     * Records the latest cumulative distance reported by the odometer.
     *
     * @param distance the cumulative distance travelled
     */
    public void update(@RoboChartType("real") double distance) {
        this.distance = distance;
    }

    /**
     * The current cumulative distance travelled.
     *
     * @return the distance travelled since start-up
     */
    @RoboChartType("real")
    public double distanceTravelled() {
        return distance;
    }
}
