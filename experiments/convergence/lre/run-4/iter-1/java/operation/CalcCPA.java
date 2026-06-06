package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and Time at Closest
 * Point of Approach (tcpa) to the closest dynamic obstacle identified by
 * cdyn, from the obstacle's relative position and velocity (LRE-OP5).
 */
public final class CalcCPA {

    private final Sensor sensor;
    private final CalcCDyn calcCDyn;

    @RoboChartType("real")
    private double relNs;

    @RoboChartType("real")
    private double relEw;

    @RoboChartType("real")
    private double closingNsVel;

    @RoboChartType("real")
    private double closingEwVel;

    @RoboChartType("real")
    private double closingSpeedSq;

    @RoboChartType("real")
    private double tcpa;

    @RoboChartType("real")
    private double cda;

    public CalcCPA(Sensor sensor, CalcCDyn calcCDyn) {
        this.sensor = sensor;
        this.calcCDyn = calcCDyn;
    }

    public void compute() {
        this.relNs = sensor.nsRelDist(calcCDyn.cdyn());
        this.relEw = sensor.ewRelDist(calcCDyn.cdyn());
        this.closingNsVel = sensor.obsNsVel(calcCDyn.cdyn());
        this.closingEwVel = sensor.obsEwVel(calcCDyn.cdyn());
        this.closingSpeedSq = this.closingNsVel * this.closingNsVel + this.closingEwVel * this.closingEwVel;
        this.tcpa = -(this.relNs * this.closingNsVel + this.relEw * this.closingEwVel) / this.closingSpeedSq;
        this.cda = Math.sqrt(
                (this.relNs + this.closingNsVel * this.tcpa) * (this.relNs + this.closingNsVel * this.tcpa)
                + (this.relEw + this.closingEwVel * this.tcpa) * (this.relEw + this.closingEwVel * this.tcpa));
    }

    public double cda() {
        return cda;
    }

    public double tcpa() {
        return tcpa;
    }
}
