package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/** The Vehicle's odometer (CD-Evt3). */
public final class VehicleSensor {

    @RoboChartType("real") private double distance = 0.0;

    /** Records the cumulative distance the Vehicle reports (never negative). */
    public void updateOdometer(@RoboChartType("real") double distance) {
        if (distance < 0.0) {
            throw new IllegalArgumentException("distance must not be negative");
        }
        this.distance = distance;
    }

    /** The cumulative distance travelled, as last reported (CD-Evt3). */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }
}
