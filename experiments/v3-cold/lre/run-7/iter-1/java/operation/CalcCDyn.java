package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/** CalcCDyn: the index of the closest dynamic obstacle. */
public final class CalcCDyn {

    private final Sensor sensor;
    @RoboChartType("nat") private int cdyn;

    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cdyn from the sensor's obstacle register. */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /** Index of the closest dynamic obstacle; 0 when there is none. */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
