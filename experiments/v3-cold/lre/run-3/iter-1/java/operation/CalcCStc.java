package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/** Finds the index of the closest static obstacle (NO_OBSTACLE if there is none). */
public final class CalcCStc {

    private final Sensor sensor;
    @RoboChartType("nat") private int cstc;

    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Refreshes cstc from the current sensor reading. */
    public void compute() {
        this.cstc = sensor.closestStaticIndex();
    }

    /** Index of the closest static obstacle. */
    @RoboChartType("nat") public int cstc() {
        return cstc;
    }
}
