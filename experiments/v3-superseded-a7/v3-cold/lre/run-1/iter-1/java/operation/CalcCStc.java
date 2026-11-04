package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Finds the index of the closest static obstacle (LRE-OP3; variable LRE-Var5).
 */
public final class CalcCStc {

    private final Sensor sensor;

    @RoboChartType("nat") private int cstc;

    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** cstc = index of the static obstacle with the least odist, or NO_OBSTACLE. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle; NO_OBSTACLE if there is none. */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
