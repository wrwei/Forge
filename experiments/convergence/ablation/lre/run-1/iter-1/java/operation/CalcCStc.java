package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Sets cstc to the index of the closest static obstacle, or -1 if no
 * static obstacle exists (LRE-OP3).
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
