package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/** Finds the index of the closest dynamic obstacle (NO_OBSTACLE if there is none). */
public final class CalcCDyn {

    private final Sensor sensor;
    @RoboChartType("nat") private int cdyn;

    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Refreshes cdyn from the current sensor reading. */
    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
    }

    /** Index of the closest dynamic obstacle. */
    @RoboChartType("nat") public int cdyn() {
        return cdyn;
    }
}
