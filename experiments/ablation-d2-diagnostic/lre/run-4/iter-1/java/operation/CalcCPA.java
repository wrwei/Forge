package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and the Time at
 * Closest Point of Approach (tcpa) to the closest dynamic obstacle
 * (LRE-OP5).
 *
 * <p>cda is expanded from the squared relative position at the closest
 * point of approach, so that the horizontal distance reported by the
 * Sensor carries the no-obstacle safe default straight through.
 */
public final class CalcCPA {

    /** Floor on the squared relative speed, keeping tcpa finite. */
    @RoboChartType("real")
    private static final double MIN_REL_SPEED_SQ = 0.000001;

    private final Sensor sensor;
    private final CalcCDyn calcCDyn;

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
        this.relNsVel = sensor.obsNsVel(calcCDyn.cdyn()) - sensor.nsVel();
        this.relEwVel = sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ewVel();
        this.relSpeedSq = this.relNsVel * this.relNsVel + this.relEwVel * this.relEwVel;
        this.closingRate = sensor.nsRelDist(calcCDyn.cdyn()) * this.relNsVel
                + sensor.ewRelDist(calcCDyn.cdyn()) * this.relEwVel;
        this.tcpa = -this.closingRate / (this.relSpeedSq + MIN_REL_SPEED_SQ);
        this.cda = Math.sqrt(sensor.hdist(calcCDyn.cdyn()) * sensor.hdist(calcCDyn.cdyn())
                + 2.0 * this.tcpa * this.closingRate
                + this.tcpa * this.tcpa * this.relSpeedSq);
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
