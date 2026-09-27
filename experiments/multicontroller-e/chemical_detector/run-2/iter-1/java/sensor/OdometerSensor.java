package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** The Vehicle's odometer: cumulative distance travelled (CD-Evt3). */
public final class OdometerSensor {

    @RoboChartType("real")
    private double distance;

    /** Records the latest cumulative distance reported by the platform. */
    public void update(@RoboChartType("real") double distance) {
        this.distance = distance;
    }

    /** Current cumulative distance travelled. */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }
}
