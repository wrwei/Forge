package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Finds the closest dynamic obstacle (LRE-OP4).
 */
public final class CalcCDyn {

    private final Sensor sensor;

    @RoboChartType("nat") private int cdyn;

    /** Creates the operation over the given sensor. */
    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cdyn. */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /**
     * Index of the closest dynamic obstacle (LRE-Var6); 0
     * ({@code ObstacleRegister.NO_OBSTACLE}) when there is none.
     */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
