package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** The Vehicle's odometer: cumulative distance travelled. */
public final class VehicleSensor {

    @RoboChartType("real") private double distance = 0.0;

    /** Records the odometer value reported by the Vehicle. */
    public void update(@RoboChartType("real") double distance) {
        this.distance = distance;
    }

    /** The current cumulative distance travelled. */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }
}
