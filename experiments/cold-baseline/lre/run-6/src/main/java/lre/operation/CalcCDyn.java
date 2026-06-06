package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * LRE-OP4: sets cdyn to the index of the closest dynamic obstacle by
 * calling sensor.closestDynamicIndex(). Returns -1 if no dynamic
 * obstacle exists.
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
