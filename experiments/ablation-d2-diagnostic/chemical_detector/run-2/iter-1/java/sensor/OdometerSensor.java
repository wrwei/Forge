package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** Latest cumulative distance travelled, as reported by the Vehicle's odometer. */
public final class OdometerSensor {

    @RoboChartType("real")
    private double distance;

    /** Cumulative distance travelled; {@code 0.0} before the first report. */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }

    /** Records a new odometer report. */
    public void update(@RoboChartType("real") double travelled) {
        this.distance = travelled;
    }
}
