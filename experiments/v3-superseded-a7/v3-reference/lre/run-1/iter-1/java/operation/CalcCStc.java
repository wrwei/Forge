package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Index of the closest static obstacle (LRE-OP3).
 */
public final class CalcCStc {

    private final Sensor sensor;
    @RoboChartType("nat") private int cstc;

    /** An operation reading the obstacle register through {@code sensor}. */
    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cstc from the sensor. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle (LRE-Var5). */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
