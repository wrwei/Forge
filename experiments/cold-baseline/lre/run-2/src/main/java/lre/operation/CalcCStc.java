package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcCStc (LRE-OP3) — sets {@code cstc} to the index of the closest
 * static obstacle, or -1 if none exists.
 */
public final class CalcCStc {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cstc = -1;

    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
