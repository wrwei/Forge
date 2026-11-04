package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/**
 * Odometer of the Vehicle: cumulative distance travelled.
 */
public final class OdometerSensor {
    @RoboChartType("real") private double distance = 0.0;

    /** Records the latest odometer value reported by the Vehicle. */
    public void update(@RoboChartType("real") double distance) {
        this.distance = distance;
    }

    /** Cumulative distance travelled (distance units). */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }
}
