package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Finds the closest static obstacle (LRE-OP3).
 */
public final class CalcCStc {

    private final Sensor sensor;

    @RoboChartType("nat") private int cstc;

    /** Creates the operation over the given sensor. */
    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cstc. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /**
     * Index of the closest static obstacle (LRE-Var5); 0
     * ({@code ObstacleRegister.NO_OBSTACLE}) when there is none.
     */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
