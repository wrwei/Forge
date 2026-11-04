package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** The Vehicle's odometer: cumulative distance travelled (CD-Evt3). */
public final class OdometerSensor {

    @RoboChartType("real") private double distance = 0.0;

    /**
     * Records a new odometer report from the Vehicle.
     *
     * @param distance the cumulative distance travelled
     */
    public void report(@RoboChartType("real") double distance) {
        this.distance = distance;
    }

    /**
     * The most recently reported cumulative distance travelled.
     *
     * @return the distance travelled
     */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }
}
