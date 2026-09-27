package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Selects the closest static obstacle (LRE-OP3, LRE-Var5). The value is -1
 * when the register holds no static obstacle.
 */
public final class CalcCStc {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cstc;

    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes the closest-static-obstacle index. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle. */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
