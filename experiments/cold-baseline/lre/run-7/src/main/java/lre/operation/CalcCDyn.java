package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Sets cdyn to the index of the closest dynamic obstacle (LRE-OP4).
 * Returns -1 if no dynamic obstacles exist.
 */
public final class CalcCDyn {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cdyn;

    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    @RoboChartType("nat")
    public int cdyn() { return cdyn; }
}
