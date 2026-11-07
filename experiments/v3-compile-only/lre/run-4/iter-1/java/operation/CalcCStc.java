package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Finds the closest static obstacle (LRE-OP3).
 */
public final class CalcCStc {

    private final Sensor sensor;

    @RoboChartType("nat") private int cstc = Sensor.NO_OBSTACLE;

    /** Creates the operation over the given sensor. */
    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cstc from the current obstacle register. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /**
     * Index of the closest static obstacle, or {@link Sensor#NO_OBSTACLE} if
     * there is none (LRE-Var5).
     */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
