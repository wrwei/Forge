package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * LRE-OP3: finds the index of the closest static obstacle (cstc).
 */
public final class CalcCStc {

    private final Sensor sensor;

    @RoboChartType("nat") private int cstc;

    /** The operation over the given sensor. */
    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cstc from the sensor. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** LRE-Var5: index of the closest static obstacle, or the register's "no obstacle" index. */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
