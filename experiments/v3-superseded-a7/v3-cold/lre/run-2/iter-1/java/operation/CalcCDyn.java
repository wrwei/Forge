package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcCDyn (LRE-OP4): index of the closest dynamic obstacle.
 */
public final class CalcCDyn {

    private final Sensor sensor;
    @RoboChartType("nat") private int cdyn;

    /** CalcCDyn reading the given sensor. */
    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cdyn from the sensor. */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /** Index of the closest dynamic obstacle; 0 when there is none (LRE-Var6). */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
