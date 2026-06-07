package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and the Time at
 * Closest Point of Approach (tcpa) to the closest dynamic obstacle
 * identified by cdyn (LRE-OP5). The obstacle's position and velocity
 * are taken relative to the AUV from the Sensor; the
 * no-dynamic-obstacle case is handled by the Sensor layer defaults.
 */
public final class CalcCPA {

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
    private double tcpa;
    @RoboChartType("real")
    private double cda;

    public CalcCPA(Sensor sensor, CalcCDyn calcCDyn) {
        this.sensor = sensor;
        this.calcCDyn = calcCDyn;
    }

    public void compute() {
        this.relNsDist = sensor.nsRelDist(calcCDyn.cdyn());
        this.relEwDist = sensor.ewRelDist(calcCDyn.cdyn());
        this.relNsVel = sensor.obsNsVel(calcCDyn.cdyn());
        this.relEwVel = sensor.obsEwVel(calcCDyn.cdyn());
        this.tcpa = (0.0 - (this.relNsDist * this.relNsVel + this.relEwDist * this.relEwVel))
                / (this.relNsVel * this.relNsVel + this.relEwVel * this.relEwVel);
        this.cda = Math.sqrt(
                (this.relNsDist + this.relNsVel * this.tcpa) * (this.relNsDist + this.relNsVel * this.tcpa)
                + (this.relEwDist + this.relEwVel * this.tcpa) * (this.relEwDist + this.relEwVel * this.tcpa));
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
