package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest forward-facing IR distance reading.
 */
public final class Sensor {

    @RoboChartType("real")
    private static final double NO_READING_DISTANCE = 1.0e6;

    @RoboChartType("real")
    private double reading = NO_READING_DISTANCE;

    /**
     * Records a new IR measurement in metres. A negative or NaN measurement is
     * not a valid reading and is treated as "no reading available".
     */
    public void update(@RoboChartType("real") double measurement) {
        if (Double.isNaN(measurement) || measurement < 0.0) {
            this.reading = NO_READING_DISTANCE;
        } else {
            this.reading = measurement;
        }
    }

    /** Discards the current reading, as when the IR is not initialised. */
    public void clear() {
        this.reading = NO_READING_DISTANCE;
    }

    /**
     * Latest IR distance in metres, non-negative. Returns a large default when
     * no reading is available so that obstacle detection is not falsely triggered.
     */
    @RoboChartType("real")
    public double distance() {
        return reading;
    }
}
