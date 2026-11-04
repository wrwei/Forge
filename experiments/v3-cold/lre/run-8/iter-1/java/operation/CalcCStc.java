package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/** Finds the index of the closest static obstacle. */
public final class CalcCStc {

    private final Sensor sensor;
    @RoboChartType("nat") private int cstc;

    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** cstc = the sensor's closest static obstacle index (0 when there is none). */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle; 0 means no static obstacle exists. */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
