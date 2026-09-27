package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Exposes the latest forward-facing IR distance reading.
 */
public final class Sensor {

    @RoboChartType("real")
    private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real")
    private double latestDistance = NO_READING_DISTANCE;

    /** Records a new IR measurement in metres; negative readings are clamped to zero. */
    public void update(@RoboChartType("real") double reading) {
        this.latestDistance = Math.max(0.0, reading);
    }

    /** Latest IR distance in metres, or a large default when no reading is available. */
    @RoboChartType("real")
    public double distance() {
        return latestDistance;
    }
}
