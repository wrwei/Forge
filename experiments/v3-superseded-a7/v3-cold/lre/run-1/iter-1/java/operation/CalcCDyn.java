package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Finds the index of the closest dynamic obstacle (LRE-OP4; variable LRE-Var6).
 */
public final class CalcCDyn {

    private final Sensor sensor;

    @RoboChartType("nat") private int cdyn;

    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** cdyn = index of the dynamic obstacle with the least odist, or NO_OBSTACLE. */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /** Index of the closest dynamic obstacle; NO_OBSTACLE if there is none. */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
