package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Forward-facing infrared distance sensor (SR-DM5, SR-SF1). Exposes the
 * latest IR distance reading in metres. When no reading is available the
 * sensor returns a large default value so the obstacle-detection
 * condition is not falsely triggered by missing data.
 */
public final class IrSensor {

    @RoboChartType("real")
    private static final double NO_READING_DEFAULT = 100.0;

    @RoboChartType("real")
    private double latestDistance = NO_READING_DEFAULT;

    /** Records a new IR distance measurement (metres, non-negative). */
    public void update(@RoboChartType("real") double measurement) {
        this.latestDistance = measurement;
    }

    /** Latest IR distance reading in metres; large default when uninitialised. */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
