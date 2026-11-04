package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcCStc: the index of the closest static obstacle (cstc), or
 * {@code ObstacleRegister.NO_OBSTACLE} when there is none.
 */
public final class CalcCStc {

    private final Sensor sensor;
    @RoboChartType("nat") private int cstc;

    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cstc from the current obstacle register. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle. */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
