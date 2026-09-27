package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Sets {@code cdyn} to the index of the closest dynamic obstacle, or
 * {@code -1} if no dynamic obstacles exist.
 */
public final class CalcCDyn {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cdyn;

    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
        this.cdyn = 0;
    }

    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
