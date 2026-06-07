package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and Time at Closest
 * Point of Approach (tcpa) to the closest dynamic obstacle (LRE-OP5).
 * The no-dynamic-obstacle case is handled by the Sensor layer, which
 * returns zero-velocity and large-distance defaults.
 */
public final class CalcCPA {

    private final Sensor sensor;
    private final CalcCDyn calcCDyn;

    @RoboChartType("real")
    private double relNs;

    @RoboChartType("real")
    private double relEw;

    @RoboChartType("real")
    private double relVelNs;

    @RoboChartType("real")
    private double relVelEw;

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
        this.relNs = sensor.nsRelDist(calcCDyn.cdyn());
        this.relEw = sensor.ewRelDist(calcCDyn.cdyn());
        this.relVelNs = sensor.obsNsVel(calcCDyn.cdyn()) - sensor.nsVel();
        this.relVelEw = sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ewVel();
        this.relSpeedSq = this.relVelNs * this.relVelNs
                + this.relVelEw * this.relVelEw;
        if (this.relSpeedSq > 0.0) {
            this.tcpa = -(this.relNs * this.relVelNs + this.relEw * this.relVelEw)
                    / this.relSpeedSq;
        } else {
            this.tcpa = -1.0;
        }
        if (this.tcpa >= 0.0) {
            this.cda = Math.sqrt(
                    (this.relNs + this.relVelNs * this.tcpa)
                            * (this.relNs + this.relVelNs * this.tcpa)
                            + (this.relEw + this.relVelEw * this.tcpa)
                                    * (this.relEw + this.relVelEw * this.tcpa));
        } else {
            this.cda = Math.sqrt(this.relNs * this.relNs + this.relEw * this.relEw);
        }
    }

    public double cda() {
        return cda;
    }

    public double tcpa() {
        return tcpa;
    }
}
