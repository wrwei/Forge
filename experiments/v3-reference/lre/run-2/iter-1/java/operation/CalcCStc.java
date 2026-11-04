package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Finds the closest static obstacle (LRE-OP3).
 */
public final class CalcCStc {

    private final Sensor sensor;
    @RoboChartType("nat") private int cstc;

    /** An operation reading from {@code sensor}. */
    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** cstc = sensor.closestStaticIndex(). */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle, or ObstacleRegister.NO_OBSTACLE (LRE-Var5). */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
