package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** The Vehicle's odometer (CD-Evt3): the cumulative distance travelled. */
public final class OdometerSensor {
    @RoboChartType("real") private double travelled = 0.0;

    /** Records the Vehicle's latest odometer report. */
    public void update(@RoboChartType("real") double distance) {
        this.travelled = distance;
    }

    /** The cumulative distance travelled, as last reported. */
    @RoboChartType("real")
    public double odometer() {
        return travelled;
    }
}
