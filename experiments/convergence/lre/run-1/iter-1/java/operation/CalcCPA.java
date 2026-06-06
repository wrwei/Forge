package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and Time at Closest
 * Point of Approach (tcpa) to the closest dynamic obstacle (LRE-OP5).
 * The relative-motion geometry lives in the Sensor layer, which also
 * supplies safe defaults when no dynamic obstacle exists.
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

    /** Recomputes cda and tcpa for the closest dynamic obstacle. */
    public void compute() {
        this.cda = sensor.cdaTo(calcCDyn.cdyn());
        this.tcpa = sensor.tcpaTo(calcCDyn.cdyn());
    }

    /** Closest distance of approach, metres. */
    public double cda() {
        return cda;
    }

    /** Time at closest point of approach, seconds. */
    public double tcpa() {
        return tcpa;
    }
}
