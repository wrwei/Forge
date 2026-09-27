package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** Sampled sensor values of the Vehicle read by the movement subsystem. */
public final class VehicleSensors {

    @RoboChartType("real")
    private double distanceTravelled;

    /** Records the latest cumulative distance reported by the odometer. */
    public void updateOdometer(@RoboChartType("real") double distance) {
        this.distanceTravelled = distance;
    }

    /** Cumulative distance travelled by the Vehicle. */
    @RoboChartType("real")
    public double odometer() {
        return distanceTravelled;
    }
}
