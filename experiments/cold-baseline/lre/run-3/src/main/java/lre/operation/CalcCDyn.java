package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * LRE-OP4: Index of the closest dynamic obstacle. -1 if none exists.
 */
public final class CalcCDyn {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cdyn;

    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
        this.cdyn = -1;
    }

    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    @RoboChartType("nat")
    public int cdyn() { return cdyn; }
}
