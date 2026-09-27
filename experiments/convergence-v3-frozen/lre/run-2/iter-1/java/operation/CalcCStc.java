package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * LRE-OP3: records the index of the closest static obstacle. Invoked once per
 * step before the transition guards.
 */
public final class CalcCStc {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cstc;

    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** LRE-Var5: index of the closest static obstacle, -1 when there is none. */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
