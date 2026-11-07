package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Finds the closest dynamic obstacle (LRE-OP4).
 */
public final class CalcCDyn {

    private final Sensor sensor;

    @RoboChartType("nat") private int cdyn = Sensor.NO_OBSTACLE;

    /** Creates the operation over the given sensor. */
    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cdyn from the current obstacle register. */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /**
     * Index of the closest dynamic obstacle, or {@link Sensor#NO_OBSTACLE} if
     * there is none (LRE-Var6).
     */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
