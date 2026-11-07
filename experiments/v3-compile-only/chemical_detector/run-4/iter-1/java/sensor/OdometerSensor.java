package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;

/**
 * The Vehicle's odometer (CD-Evt3): the cumulative distance travelled,
 * sampled by the movement subsystem into d0 and d1 for stuck detection.
 */
public final class OdometerSensor {

    @RoboChartType("real") private double distance = 0.0;

    /**
     * Records the cumulative distance reported by the Vehicle. Cumulative
     * distance never decreases; a smaller report is ignored.
     */
    public void update(@RoboChartType("real") double cumulativeDistance) {
        if (cumulativeDistance > distance) {
            distance = cumulativeDistance;
        }
    }

    /** The cumulative distance travelled. */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }
}
