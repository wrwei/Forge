package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Finds the closest static obstacle.
 */
public final class CalcCStc {
    private final Sensor sensor;
    @RoboChartType("nat") private int cstc;

    public CalcCStc(Sensor sensor) { this.sensor = sensor; }

    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle. */
    @RoboChartType("nat") public int cstc() { return cstc; }
}
