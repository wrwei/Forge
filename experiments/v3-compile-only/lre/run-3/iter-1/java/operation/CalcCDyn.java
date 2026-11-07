package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcCDyn (LRE-OP4): the index of the closest dynamic obstacle (LRE-Var6).
 * Index 0 means that no dynamic obstacle exists.
 */
public final class CalcCDyn {

    private final Sensor sensor;
    @RoboChartType("nat") private int cdyn;

    /** Creates the operation over the given sensor. */
    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cdyn from the current obstacle register. */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /** Index of the closest dynamic obstacle, or 0 if none. */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
