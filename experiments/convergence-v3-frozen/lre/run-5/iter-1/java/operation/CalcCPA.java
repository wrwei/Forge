package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and the Time at Closest Point
 * of Approach (tcpa) to the closest dynamic obstacle.
 */
public final class CalcCPA {

    private final Sensor sensor;
    private final CalcCDyn calcCDyn;

    @RoboChartType("nat")
    private int cdyn;

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

    public void compute() {
        this.cdyn = calcCDyn.cdyn();
        this.relNsVel = sensor.obsNsVel(this.cdyn) - sensor.nsVel();
        this.relEwVel = sensor.obsEwVel(this.cdyn) - sensor.ewVel();
        this.relSpeedSq = this.relNsVel * this.relNsVel + this.relEwVel * this.relEwVel;
        this.closingRate = sensor.nsRelDist(this.cdyn) * this.relNsVel
                + sensor.ewRelDist(this.cdyn) * this.relEwVel;
        this.hdistCdyn = sensor.hdist(this.cdyn);
        this.tcpa = (0.0 - this.closingRate) / this.relSpeedSq;
        this.cda = Math.sqrt(this.hdistCdyn * this.hdistCdyn
                - this.closingRate * this.closingRate / this.relSpeedSq);
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
