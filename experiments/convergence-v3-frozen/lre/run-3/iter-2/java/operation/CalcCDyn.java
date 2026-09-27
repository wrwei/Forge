package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/** Selects the closest dynamic obstacle. */
public final class CalcCDyn {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cdyn = -1;

    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes the closest-dynamic-obstacle index from the current sensor reading. */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /** Index of the closest dynamic obstacle, or -1 if there is none. */
    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }
}
