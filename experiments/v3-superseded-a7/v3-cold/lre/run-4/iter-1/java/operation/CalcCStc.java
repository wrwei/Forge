package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/** The CalcCStc operation (LRE-OP3): the index of the closest static obstacle. */
public final class CalcCStc {

    private final Sensor sensor;
    @RoboChartType("nat") private int cstc;

    /** CalcCStc over the given sensor. */
    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Computes cstc. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle; ObstacleRegister.NO_OBSTACLE if none (LRE-Var5). */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
