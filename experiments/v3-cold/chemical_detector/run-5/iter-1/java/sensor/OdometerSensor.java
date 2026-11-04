package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** The Vehicle's odometer (CD-Evt3): cumulative distance travelled. */
public final class OdometerSensor {

    @RoboChartType("real") private double distance = 0.0;

    /** Records the latest odometer report from the Vehicle. */
    public void update(@RoboChartType("real") double distance) {
        this.distance = distance;
    }

    /** Current cumulative distance travelled. */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }
}
