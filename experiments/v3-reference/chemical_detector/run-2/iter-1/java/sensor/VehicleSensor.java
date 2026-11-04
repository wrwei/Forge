package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/**
 * The Vehicle's odometer (CD-Evt3): the cumulative distance travelled.
 */
public final class VehicleSensor {

    @RoboChartType("real") private double distance = 0.0;

    /** Records the latest odometer reading reported by the Vehicle. */
    public void update(@RoboChartType("real") double distance) {
        this.distance = distance;
    }

    /** CD-Evt3: the cumulative distance the Vehicle has travelled. */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }
}
