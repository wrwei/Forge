package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Selects the closest dynamic obstacle (LRE-OP4).
 */
public final class CalcCDyn {

    private final Sensor sensor;
    @RoboChartType("nat") private int cdyn;

    /** Creates the operation over the given sensor. */
    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** cdyn = index of the dynamic obstacle with the least odist. */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /** Index of the closest dynamic obstacle (LRE-Var6). */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
