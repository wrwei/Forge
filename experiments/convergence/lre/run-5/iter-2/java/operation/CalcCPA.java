package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and Time at Closest Point
 * of Approach (tcpa) to the closest dynamic obstacle identified by cdyn
 * (LRE-OP5). Uses the obstacle's horizontal relative position and velocity
 * from the Sensor. When the relative speed is zero (including the
 * no-dynamic-obstacle case), tcpa defaults to -1 and cda falls back to the
 * horizontal distance, which is a safe large distance when no obstacle
 * exists.
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
    private double speedSq;
    @RoboChartType("real")
    private double cda;
    @RoboChartType("real")
    private double tcpa;

    public CalcCPA(Sensor sensor, CalcCDyn calcCDyn) {
        this.sensor = sensor;
        this.calcCDyn = calcCDyn;
    }

    public void compute() {
        this.relNs = sensor.nsRelDist(calcCDyn.cdyn());
        this.relEw = sensor.ewRelDist(calcCDyn.cdyn());
        this.relVelNs = sensor.obsNsVel(calcCDyn.cdyn());
        this.relVelEw = sensor.obsEwVel(calcCDyn.cdyn());
        this.speedSq = this.relVelNs * this.relVelNs + this.relVelEw * this.relVelEw;
        if (this.speedSq > 0.0) {
            this.tcpa = -(this.relNs * this.relVelNs + this.relEw * this.relVelEw) / this.speedSq;
        } else {
            this.tcpa = -1.0;
        }
        if (this.speedSq > 0.0 && this.tcpa > 0.0) {
            this.cda = Math.sqrt(
                    (this.relNs + this.relVelNs * this.tcpa) * (this.relNs + this.relVelNs * this.tcpa)
                    + (this.relEw + this.relVelEw * this.tcpa) * (this.relEw + this.relVelEw * this.tcpa));
        } else if (this.speedSq > 0.0) {
            this.cda = Math.sqrt(this.relNs * this.relNs + this.relEw * this.relEw);
        } else {
            this.cda = sensor.hdist(calcCDyn.cdyn());
        }
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
