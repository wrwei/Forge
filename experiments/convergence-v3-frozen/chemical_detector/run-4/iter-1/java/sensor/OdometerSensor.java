package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** Odometer: cumulative distance travelled by the Vehicle. */
public final class OdometerSensor {

    @RoboChartType("real")
    private double cumulativeDistance;

    public void update(@RoboChartType("real") double distance) {
        this.cumulativeDistance = distance;
    }

    @RoboChartType("real")
    public double distanceTravelled() {
        return cumulativeDistance;
    }
}
