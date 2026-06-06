package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and Time at Closest Point
 * of Approach (tcpa) to the closest dynamic obstacle identified by cdyn
 * (LRE-OP5). The no-dynamic-obstacle case is handled by the Sensor layer.
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

    public void compute() {
        this.tcpa = sensor.cpaTime(calcCDyn.cdyn());
        this.cda = sensor.cpaDist(calcCDyn.cdyn());
    }

    @RoboChartType("real")
    public double cda() {
        return cda;
    }

    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }
}
