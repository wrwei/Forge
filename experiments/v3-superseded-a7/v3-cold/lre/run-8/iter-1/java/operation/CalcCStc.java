package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Finds the closest static obstacle.
 */
public final class CalcCStc {

    private final Sensor sensor;
    @RoboChartType("nat") private int cstc;

    /** Creates the operation over the given sensor. */
    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** cstc = index of the static obstacle with the least odist (0 if there is none). */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle. */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
