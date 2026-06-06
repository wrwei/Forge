package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest IR distance reading to the controller (SR-DM5, SR-SF1).
 * When no reading is available the sensor returns a large default so the
 * obstacle-detection condition is not falsely triggered by missing data.
 */
public final class Sensor {

    /** Large default returned before any real reading has arrived, metres. */
    @RoboChartType("real")
    private static final double NO_READING_DEFAULT = 1000.0;

    @RoboChartType("real")
    private double lastReading = NO_READING_DEFAULT;

    /** Latest IR distance reading in metres, non-negative (SR-SF1). */
    @RoboChartType("real")
    public double distance() {
        return lastReading;
    }

    /** Records a fresh IR distance reading from the sensor framework. */
    public void update(@RoboChartType("real") double reading) {
        this.lastReading = reading;
    }
}
