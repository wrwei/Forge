package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and the Time at Closest
 * Point of Approach (tcpa) with respect to the closest dynamic obstacle.
 *
 * <p>The relative closing speed is floored by a small positive term so the
 * quotients stay defined when the AUV and the obstacle are not closing. In
 * that degenerate case tcpa is zero and cda collapses to the present
 * horizontal distance, which the Sensor reports as a large safe value when no
 * dynamic obstacle exists.
 */
public final class CalcCPA {

    private final Sensor sensor;

    private final CalcCDyn calcCDyn;

    @RoboChartType("nat")
    private int closestDynamic = -1;

    @RoboChartType("real")
    private double relNsVel;

    @RoboChartType("real")
    private double relEwVel;

    @RoboChartType("real")
    private double relSpeedSq;

    @RoboChartType("real")
    private double closingRate;

    @RoboChartType("real")
    private double cda;

    @RoboChartType("real")
    private double tcpa;

    public CalcCPA(Sensor sensor, CalcCDyn calcCDyn) {
        this.sensor = sensor;
        this.calcCDyn = calcCDyn;
    }

    public void compute() {
        this.closestDynamic = calcCDyn.cdyn();
        this.relNsVel = sensor.obsNsVel(this.closestDynamic) - sensor.nsVel();
        this.relEwVel = sensor.obsEwVel(this.closestDynamic) - sensor.ewVel();
        this.relSpeedSq = this.relNsVel * this.relNsVel + this.relEwVel * this.relEwVel + 1.0e-9;
        this.closingRate = sensor.nsRelDist(this.closestDynamic) * this.relNsVel
                + sensor.ewRelDist(this.closestDynamic) * this.relEwVel;
        this.tcpa = (0.0 - this.closingRate) / this.relSpeedSq;
        this.cda = Math.sqrt(sensor.hdist(this.closestDynamic) * sensor.hdist(this.closestDynamic)
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
