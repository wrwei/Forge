package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Finds the index of the closest static obstacle.
 */
public final class CalcCStc {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cstc = 0;

    /**
     * Creates the operation over a sensor.
     *
     * @param sensor the AUV's sensor
     */
    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cstc from the current obstacle register. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /**
     * Index of the closest static obstacle.
     *
     * @return cstc; the sensor's no-obstacle index when there is no static obstacle
     */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
