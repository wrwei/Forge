package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Finds the closest dynamic obstacle (LRE-OP4).
 */
public final class CalcCDyn {

    private final Sensor sensor;
    @RoboChartType("nat") private int cdyn;

    /** An operation reading from {@code sensor}. */
    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** cdyn = sensor.closestDynamicIndex(). */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /** Index of the closest dynamic obstacle, or ObstacleRegister.NO_OBSTACLE (LRE-Var6). */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
