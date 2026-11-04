package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/** Finds the index of the closest dynamic obstacle. */
public final class CalcCDyn {

    private final Sensor sensor;
    @RoboChartType("nat") private int cdyn;

    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** cdyn = the sensor's closest dynamic obstacle index (0 when there is none). */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /** Index of the closest dynamic obstacle; 0 means no dynamic obstacle exists. */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
