package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** The odometer: cumulative distance travelled by the Vehicle (CD-Evt3). */
public final class OdometerSensor {
    @RoboChartType("real") private double distance = 0.0;

    /**
     * Records the latest odometer report from the Vehicle.
     *
     * @param distance cumulative distance travelled
     */
    public void update(@RoboChartType("real") double distance) {
        this.distance = distance;
    }

    /**
     * The current cumulative distance travelled.
     *
     * @return the distance travelled since start-up
     */
    @RoboChartType("real")
    public double distanceTravelled() {
        return distance;
    }
}
