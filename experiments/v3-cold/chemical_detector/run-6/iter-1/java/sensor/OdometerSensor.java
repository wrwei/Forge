package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** The Vehicle's odometer: cumulative distance travelled (CD-Evt3). */
public final class OdometerSensor {
    @RoboChartType("real") private double travelled = 0.0;

    /** Records the latest cumulative distance reported by the Vehicle. */
    public void update(@RoboChartType("real") double cumulative) {
        this.travelled = cumulative;
    }

    /** The cumulative distance travelled so far. */
    @RoboChartType("real")
    public double distance() {
        return travelled;
    }
}
