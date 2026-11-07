package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Selects the closest static obstacle (LRE-OP3).
 */
public final class CalcCStc {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cstc;

    /**
     * @param sensor the AUV sensor service
     */
    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cstc. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** @return identifier of the closest static obstacle */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
