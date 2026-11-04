package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** The Vehicle's odometer: the cumulative distance travelled (CD-Evt3). */
public final class OdometerSensor {

    @RoboChartType("real") private double distance = 0.0;

    /** Records the latest cumulative distance reported by the odometer. */
    public void update(@RoboChartType("real") double travelled) {
        this.distance = travelled;
    }

    /** The current cumulative distance travelled. */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }
}
