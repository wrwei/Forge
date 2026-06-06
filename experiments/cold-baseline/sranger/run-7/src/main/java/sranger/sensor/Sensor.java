package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Sensor for the SRanger controller (SR-DM5, SR-SF1).
 *
 * Exposes the latest IR distance reading. When no reading is available, returns
 * a large default value so the obstacle-detection condition is not falsely
 * triggered by missing data.
 */
public final class Sensor {

    /** Large default value returned when no IR reading is available. */
    @RoboChartType("real")
    private static final double NO_READING_DEFAULT = 1.0e6;

    @RoboChartType("real")
    private double currentDistance = NO_READING_DEFAULT;

    private boolean initialised = false;

    /**
     * Update the latest IR distance reading (in metres, non-negative).
     */
    public void update(@RoboChartType("real") double newDistance) {
        this.currentDistance = newDistance;
        this.initialised = true;
    }

    /**
     * Latest IR distance reading in metres.
     * Returns a large default value when no reading is available (SR-DM5).
     */
    @RoboChartType("real")
    public double distance() {
        if (!initialised) {
            return NO_READING_DEFAULT;
        }
        return currentDistance;
    }
}
