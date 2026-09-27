package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * LRE-OP4: records the index of the closest dynamic obstacle. Invoked once per
 * step before the transition guards.
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

    /** LRE-Var6: index of the closest dynamic obstacle, -1 when there is none. */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
