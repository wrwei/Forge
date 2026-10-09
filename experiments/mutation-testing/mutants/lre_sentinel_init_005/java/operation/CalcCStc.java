package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Sets {@code cstc} to the index of the closest static obstacle, or
 * {@code -1} if no static obstacles exist.
 */
public final class CalcCStc {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cstc;

    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
        this.cstc = 0;
    }

    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
