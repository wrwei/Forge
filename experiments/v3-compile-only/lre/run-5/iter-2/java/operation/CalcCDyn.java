package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Selects the closest dynamic obstacle (LRE-OP4).
 */
public final class CalcCDyn {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cdyn;

    /**
     * @param sensor the AUV sensor service
     */
    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cdyn. */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /** @return identifier of the closest dynamic obstacle */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
