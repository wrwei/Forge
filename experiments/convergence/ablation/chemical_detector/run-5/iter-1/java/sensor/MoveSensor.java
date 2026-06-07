package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;

/**
 * Odometry sensing for the movement subsystem (CD-Evt3). Reports the
 * cumulative distance travelled by the Vehicle.
 */
public final class MoveSensor {

    @RoboChartType("real")
    private double distance;

    public void update(@RoboChartType("real") double travelled) {
        this.distance = travelled;
    }

    /**
     * Cumulative distance travelled (CD-Evt3).
     */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }
}
