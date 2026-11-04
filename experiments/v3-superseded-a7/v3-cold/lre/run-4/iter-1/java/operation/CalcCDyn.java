package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/** The CalcCDyn operation (LRE-OP4): the index of the closest dynamic obstacle. */
public final class CalcCDyn {

    private final Sensor sensor;
    @RoboChartType("nat") private int cdyn;

    /** CalcCDyn over the given sensor. */
    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Computes cdyn. */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /** Index of the closest dynamic obstacle; ObstacleRegister.NO_OBSTACLE if none (LRE-Var6). */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
