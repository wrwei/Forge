package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;

/** Cumulative travelled distance reported by the Vehicle (CD-Evt3). */
public final class OdometerSensor {

    @RoboChartType("real")
    private double distance = 0.0;

    public void update(@RoboChartType("real") double travelled) {
        this.distance = travelled;
    }

    /** Current cumulative distance travelled. */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }
}
