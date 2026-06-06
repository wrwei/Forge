package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Sets cstc to the index of the closest static obstacle (LRE-OP3).
 * Returns -1 if no static obstacles exist.
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

    @RoboChartType("nat")
    public int cstc() { return cstc; }
}
