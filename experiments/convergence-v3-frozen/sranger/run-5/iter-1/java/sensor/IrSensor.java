package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * The forward-facing infrared distance sensor (SR-DM5, SR-SF1).
 */
public final class IrSensor {

    /**
     * Safe default returned before the first reading arrives: large enough
     * that the obstacle-detection condition is never falsely triggered by
     * missing data.
     */
    @RoboChartType("real")
    private static final double NO_READING_DISTANCE = 1000.0;

    @RoboChartType("real")
    private double distanceMetres = NO_READING_DISTANCE;

    /** Publish the latest IR measurement, in metres. */
    public void updateDistance(@RoboChartType("real") double metres) {
        this.distanceMetres = metres;
    }

    /** Latest IR distance reading in metres, non-negative (SR-SF1). */
    @RoboChartType("real")
    public double distance() {
        return distanceMetres;
    }
}
