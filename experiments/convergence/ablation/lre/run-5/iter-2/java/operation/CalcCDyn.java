package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Sets cdyn to the index of the closest dynamic obstacle, or -1 if no
 * dynamic obstacle exists. Invoked by the controller before evaluating
 * transition guards each step.
 */
public final class CalcCDyn {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cdyn = -1;

    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
