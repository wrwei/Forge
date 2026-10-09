package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Sensor interface for the SRanger controller (SR-DM5, SR-SF1).
 *
 * <p>Exposes the latest IR distance reading. When no reading is available, {@link #distance()}
 * returns a large default value so the obstacle-detection condition is not falsely
 * triggered by missing data.</p>
 */
public final class Sensor {

    /** Default reading returned when no IR measurement is available (metres). */
    @RoboChartType("real")
    public static final double NO_READING_DEFAULT = 1001.0;

    @RoboChartType("real")
    private double latestDistance = NO_READING_DEFAULT;

    /**
     * Update the cached IR reading. Negative inputs are treated as "no reading"
     * and replaced with the safe default.
     */
    public void updateDistance(@RoboChartType("real") double metres) {
        if (metres < 0.0) {
            this.latestDistance = NO_READING_DEFAULT;
        } else {
            this.latestDistance = metres;
        }
    }

    /**
     * Latest IR distance reading in metres, non-negative. Returns a large default
     * when no reading is available so the obstacle condition is not falsely tripped.
     */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
