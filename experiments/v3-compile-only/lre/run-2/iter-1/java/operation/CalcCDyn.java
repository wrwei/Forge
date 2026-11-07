package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Finds the index of the closest dynamic obstacle.
 */
public final class CalcCDyn {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cdyn = 0;

    /**
     * Creates the operation over a sensor.
     *
     * @param sensor the AUV's sensor
     */
    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cdyn from the current obstacle register. */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /**
     * Index of the closest dynamic obstacle.
     *
     * @return cdyn; the sensor's no-obstacle index when there is no dynamic obstacle
     */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
