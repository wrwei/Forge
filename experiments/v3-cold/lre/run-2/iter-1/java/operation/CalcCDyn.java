package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Selects the closest dynamic obstacle (LRE-OP4).
 */
public final class CalcCDyn {

    private final Sensor sensor;
    // 0 is ObstacleRegister.NO_OBSTACLE: no dynamic obstacle known yet.
    @RoboChartType("nat") private int cdyn = 0;

    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cdyn from the current obstacle register. */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /** Index of the closest dynamic obstacle, or ObstacleRegister.NO_OBSTACLE (LRE-Var6). */
    @RoboChartType("nat") public int cdyn() {
        return cdyn;
    }
}
