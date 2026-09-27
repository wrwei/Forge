package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and the Time at Closest
 * Point of Approach (tcpa) to the closest dynamic obstacle (LRE-OP5).
 */
public final class CalcCPA {

    private final Sensor sensor;
    private final CalcCDyn calcCDyn;

    @RoboChartType("nat")
    private int cdyn = -1;

    @RoboChartType("real")
    private double relNsVel;

    @RoboChartType("real")
    private double relEwVel;

    @RoboChartType("real")
    private double relSpeedSq;

    @RoboChartType("real")
    private double tcpa;

    @RoboChartType("real")
    private double cda;

    public CalcCPA(Sensor sensor, CalcCDyn calcCDyn) {
        this.sensor = sensor;
        this.calcCDyn = calcCDyn;
    }

    public void compute() {
        this.cdyn = calcCDyn.cdyn();
        this.relNsVel = sensor.obsNsVel(this.cdyn) - sensor.nsVel();
        this.relEwVel = sensor.obsEwVel(this.cdyn) - sensor.ewVel();
        this.relSpeedSq = this.relNsVel * this.relNsVel + this.relEwVel * this.relEwVel + 0.000001;
        this.tcpa = (0.0 - (sensor.nsRelDist(this.cdyn) * this.relNsVel
                + sensor.ewRelDist(this.cdyn) * this.relEwVel)) / this.relSpeedSq;
        this.cda = Math.sqrt(
                (sensor.nsRelDist(this.cdyn) + this.relNsVel * this.tcpa)
                        * (sensor.nsRelDist(this.cdyn) + this.relNsVel * this.tcpa)
                + (sensor.ewRelDist(this.cdyn) + this.relEwVel * this.tcpa)
                        * (sensor.ewRelDist(this.cdyn) + this.relEwVel * this.tcpa));
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
