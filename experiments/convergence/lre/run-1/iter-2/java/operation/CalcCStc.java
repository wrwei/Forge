package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Sets cstc to the index of the closest static obstacle (LRE-OP3).
 * Value -1 means no static obstacle exists.
 */
public final class CalcCStc {

    private final Sensor sensor;
    @RoboChartType("nat")
    private int cstc = -1;

    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes the closest-static-obstacle index. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle, or -1 if none. */
    public int cstc() {
        return cstc;
    }
}
