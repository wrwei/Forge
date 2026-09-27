package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach and the Time at Closest Point of
 * Approach to the closest dynamic obstacle (LRE-OP5). The relative position
 * and velocity, the no-dynamic-obstacle case and the vanishing-relative-
 * velocity case are all resolved by the Sensor layer.
 */
public final class CalcCPA {

    private final Sensor sensor;
    private final CalcCDyn calcCDyn;

    @RoboChartType("real")
    private double cda;

    @RoboChartType("real")
    private double tcpa;

    public CalcCPA(Sensor sensor, CalcCDyn calcCDyn) {
        this.sensor = sensor;
        this.calcCDyn = calcCDyn;
    }

    /** Recomputes cda and tcpa for the current cycle. */
    public void compute() {
        this.cda = sensor.cdaTo(calcCDyn.cdyn());
        this.tcpa = sensor.tcpaTo(calcCDyn.cdyn());
    }

    /** Closest Distance of Approach, in metres (LRE-Var7). */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }

    /** Time at Closest Point of Approach, in seconds (LRE-Var8). */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }
}
