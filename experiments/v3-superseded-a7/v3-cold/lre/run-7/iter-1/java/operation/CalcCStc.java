package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Selects the closest static obstacle (LRE-OP3; variable LRE-Var5).
 */
public final class CalcCStc {

    private final Sensor sensor;

    @RoboChartType("nat") private int cstc;

    /** Creates the operation over the given sensor. */
    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** cstc = index of the static obstacle with the smallest odist. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle (an index with no obstacle if there is none). */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
