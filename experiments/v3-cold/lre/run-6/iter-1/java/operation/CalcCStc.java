package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Selects the closest static obstacle (LRE-OP3).
 */
public final class CalcCStc {

    private final Sensor sensor;

    @RoboChartType("nat") private int cstc = 0;

    /** Creates the operation over the AUV's sensor. */
    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cstc. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle; 0 means none (LRE-Var5). */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
