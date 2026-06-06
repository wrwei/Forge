package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest IR distance reading (SR-DM5, SR-SF1).
 */
public final class Sensor {

    /** Large default so missing data never falsely triggers obstacle detection. */
    private static final double NO_READING_DEFAULT = 1000.0;

    @RoboChartType("real")
    private double irDistance = NO_READING_DEFAULT;

    /** Latest IR distance reading in metres, non-negative. */
    public double distance() {
        return irDistance;
    }

    /** Stores a new IR reading (called by the sensor framework). */
    public void update(double reading) {
        this.irDistance = reading;
    }
}
