package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest IR distance reading to the controller.
 */
public final class Sensor {

    private static final double NO_READING_DEFAULT = 100.0;

    @RoboChartType("real")
    private double irDistance = NO_READING_DEFAULT;

    /** Records a new IR distance measurement (metres, non-negative). */
    public void update(@RoboChartType("real") double value) {
        this.irDistance = value;
    }

    /**
     * Latest IR distance reading in metres, non-negative. Returns a large
     * default value when no reading is available, so the obstacle-detection
     * condition is not falsely triggered by missing data.
     */
    @RoboChartType("real")
    public double distance() {
        return irDistance;
    }
}
