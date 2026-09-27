package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and the Time at Closest
 * Point of Approach (tcpa) to the closest dynamic obstacle.
 */
public final class CalcCPA {

    /** Keeps the closing-speed denominator away from zero. */
    @RoboChartType("real")
    private static final double MIN_CLOSING_SPEED_SQ = 1.0e-9;

    private final Sensor sensor;
    private final CalcCDyn calcCDyn;

    @RoboChartType("nat")
    private int cdyn;

    @RoboChartType("real")
    private double relNs;

    @RoboChartType("real")
    private double relEw;

    @RoboChartType("real")
    private double relNsVel;

    @RoboChartType("real")
    private double relEwVel;

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
        this.cdyn = calcCDyn.cdyn();
        this.relNs = sensor.nsRelDist(this.cdyn);
        this.relEw = sensor.ewRelDist(this.cdyn);
        this.relNsVel = sensor.obsNsVel(this.cdyn) - sensor.nsVel();
        this.relEwVel = sensor.obsEwVel(this.cdyn) - sensor.ewVel();
        this.closingSpeedSq = this.relNsVel * this.relNsVel
                + this.relEwVel * this.relEwVel + MIN_CLOSING_SPEED_SQ;
        this.tcpa = -(this.relNs * this.relNsVel + this.relEw * this.relEwVel)
                / this.closingSpeedSq;
        this.cda = Math.sqrt(
                (this.relNs + this.relNsVel * this.tcpa) * (this.relNs + this.relNsVel * this.tcpa)
                + (this.relEw + this.relEwVel * this.tcpa) * (this.relEw + this.relEwVel * this.tcpa));
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
