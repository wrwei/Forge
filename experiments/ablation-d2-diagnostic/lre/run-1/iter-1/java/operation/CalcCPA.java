package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach and the Time at Closest Point of
 * Approach to the closest dynamic obstacle (LRE-OP5). The no-dynamic-obstacle
 * case is absorbed by the Sensor, which reports a safe large horizontal
 * distance and zero relative components when no dynamic obstacle exists.
 */
public final class CalcCPA {

    /** Keeps the relative-speed denominator strictly positive. */
    private static final double EPSILON = 1.0E-9;

    private final Sensor sensor;
    private final CalcCDyn calcCDyn;

    @RoboChartType("real")
    private double relNsDist;

    @RoboChartType("real")
    private double relEwDist;

    @RoboChartType("real")
    private double relNsVel;

    @RoboChartType("real")
    private double relEwVel;

    @RoboChartType("real")
    private double relSpeedSq;

    @RoboChartType("real")
    private double closingRate;

    @RoboChartType("real")
    private double hdistCdyn;

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
        this.relNsDist = sensor.nsRelDist(calcCDyn.cdyn());
        this.relEwDist = sensor.ewRelDist(calcCDyn.cdyn());
        this.relNsVel = sensor.obsNsVel(calcCDyn.cdyn()) - sensor.nsVel();
        this.relEwVel = sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ewVel();
        this.relSpeedSq = this.relNsVel * this.relNsVel + this.relEwVel * this.relEwVel + EPSILON;
        this.closingRate = this.relNsDist * this.relNsVel + this.relEwDist * this.relEwVel;
        this.hdistCdyn = sensor.hdist(calcCDyn.cdyn());
        this.tcpa = (0.0 - this.closingRate) / this.relSpeedSq;
        this.cda = Math.sqrt(this.hdistCdyn * this.hdistCdyn
                - this.closingRate * this.closingRate / this.relSpeedSq);
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
