package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Finds the closest static obstacle (LRE-OP3).
 */
public final class CalcCStc {

    private final Sensor sensor;
    @RoboChartType("nat") private int cstc;

    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cstc from the sensor. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /**
     * Index of the closest static obstacle, or
     * {@link lre.sensor.ObstacleRegister#NO_OBSTACLE} if none (LRE-Var5).
     */
    @RoboChartType("nat") public int cstc() { return cstc; }
}
