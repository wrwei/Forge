package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Selects the closest dynamic obstacle (LRE-OP4).
 */
public final class CalcCDyn {

    private final Sensor sensor;

    @RoboChartType("nat") private int cdyn = 0;

    /** Creates the operation over the AUV's sensor. */
    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cdyn. */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /** Index of the closest dynamic obstacle; 0 means none (LRE-Var6). */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
