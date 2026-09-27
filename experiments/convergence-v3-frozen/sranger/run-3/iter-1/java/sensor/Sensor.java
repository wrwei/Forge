package sranger.sensor;

import sranger.annotation.RoboChartType;

/**
 * Forward-facing IR distance sensor (SR-DM5).
 */
public final class Sensor {

    /**
     * Value reported before the IR sensor has produced a reading. Deliberately
     * large so that the obstacle-detection condition is not triggered by
     * missing data.
     */
    @RoboChartType("real")
    private static final double NO_READING = 1000.0;

    @RoboChartType("real")
    private double distance = NO_READING;

    /** Records the latest IR measurement, in metres. */
    public void update(@RoboChartType("real") double reading) {
        this.distance = reading;
    }

    /** Latest IR distance reading in metres; a large default when none is available (SR-SF1). */
    @RoboChartType("real")
    public double distance() {
        return distance;
    }
}
