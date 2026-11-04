package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/** CalcCStc: the index of the closest static obstacle. */
public final class CalcCStc {

    private final Sensor sensor;
    @RoboChartType("nat") private int cstc;

    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cstc from the sensor's obstacle register. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle; 0 when there is none. */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
