package sranger.sensor;

import sranger.annotation.RoboChartType;

/** Exposes the latest IR distance reading to the controller (SR-DM5, SR-SF1). */
public final class Sensor {

    /** Returned while no reading is available, so obstacle detection is not falsely triggered. */
    @RoboChartType("real")
    public static final double DEFAULT_DISTANCE = 1000.0;

    @RoboChartType("real")
    private double irDistance = DEFAULT_DISTANCE;

    /** Records a fresh IR measurement, in metres. */
    public void update(@RoboChartType("real") double metres) {
        this.irDistance = metres;
    }

    /** Latest IR distance reading in metres, non-negative (SR-SF1). */
    @RoboChartType("real")
    public double distance() {
        return irDistance;
    }
}
