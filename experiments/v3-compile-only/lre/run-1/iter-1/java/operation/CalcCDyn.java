package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * LRE-OP4: finds the index of the closest dynamic obstacle (cdyn).
 */
public final class CalcCDyn {

    private final Sensor sensor;

    @RoboChartType("nat") private int cdyn;

    /** The operation over the given sensor. */
    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cdyn from the sensor. */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /** LRE-Var6: index of the closest dynamic obstacle, or the register's "no obstacle" index. */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
