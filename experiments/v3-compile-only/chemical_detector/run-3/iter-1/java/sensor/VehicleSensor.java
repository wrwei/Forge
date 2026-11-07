package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** The Vehicle's odometer (CD-Evt3): cumulative distance travelled. */
public final class VehicleSensor {
    @RoboChartType("real") private double travelled = 0.0;

    /** Records the latest odometer value reported by the Vehicle. */
    public void updateOdometer(@RoboChartType("real") double distance) {
        this.travelled = distance;
    }

    /** Current cumulative distance travelled. */
    @RoboChartType("real")
    public double odometer() {
        return travelled;
    }
}
