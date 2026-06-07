package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;

/**
 * Cumulative distance travelled by the Vehicle (CD-Evt3). The movement
 * subsystem samples {@link #distance()} into d0/d1 for stuck detection.
 */
public final class OdometerSensor {

    @RoboChartType("real")
    private double travelled;

    public void update(@RoboChartType("real") double newDistance) {
        this.travelled = newDistance;
    }

    @RoboChartType("real")
    public double distance() {
        return travelled;
    }
}
