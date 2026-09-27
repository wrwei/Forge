package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Selects the closest dynamic obstacle (LRE-OP4, LRE-Var6). The value is -1
 * when the register holds no dynamic obstacle.
 */
public final class CalcCDyn {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cdyn = -1;

    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes the closest-dynamic-obstacle index. */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /** Index of the closest dynamic obstacle. */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
