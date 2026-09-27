package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/** Selects the closest static obstacle. */
public final class CalcCStc {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cstc = -1;

    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes the closest-static-obstacle index from the current sensor reading. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle, or -1 if there is none. */
    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }
}
