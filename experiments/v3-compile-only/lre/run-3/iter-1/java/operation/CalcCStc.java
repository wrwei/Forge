package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcCStc (LRE-OP3): the index of the closest static obstacle (LRE-Var5).
 * Index 0 means that no static obstacle exists.
 */
public final class CalcCStc {

    private final Sensor sensor;
    @RoboChartType("nat") private int cstc;

    /** Creates the operation over the given sensor. */
    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cstc from the current obstacle register. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle, or 0 if none. */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
