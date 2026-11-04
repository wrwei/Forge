package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/**
 * The Vehicle's odometer (CD-Evt3): the cumulative distance travelled, sampled by the
 * movement subsystem for stuck detection.
 */
public final class VehicleSensor {

    @RoboChartType("real") private double distance = 0.0;

    /** Records the latest odometer report. */
    public void update(@RoboChartType("real") double distance) {
        this.distance = distance;
    }

    /** The cumulative distance travelled so far. */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }
}
