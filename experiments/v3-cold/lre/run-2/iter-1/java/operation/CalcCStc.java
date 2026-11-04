package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Selects the closest static obstacle (LRE-OP3).
 */
public final class CalcCStc {

    private final Sensor sensor;
    // 0 is ObstacleRegister.NO_OBSTACLE: no static obstacle known yet.
    @RoboChartType("nat") private int cstc = 0;

    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cstc from the current obstacle register. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle, or ObstacleRegister.NO_OBSTACLE (LRE-Var5). */
    @RoboChartType("nat") public int cstc() {
        return cstc;
    }
}
