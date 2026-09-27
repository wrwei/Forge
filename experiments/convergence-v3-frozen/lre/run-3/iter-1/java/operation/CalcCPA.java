package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach and the Time at Closest Point of
 * Approach to the closest dynamic obstacle.
 */
public final class CalcCPA {

    /**
     * Floor on the squared relative speed, so that the time at closest point of
     * approach stays defined when the AUV and the obstacle are not converging.
     */
    private static final double MIN_REL_SPEED_SQ = 1.0E-9;

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
    private double cda;

    @RoboChartType("real")
    private double tcpa;

    public CalcCPA(Sensor sensor, CalcCDyn calcCDyn) {
        this.sensor = sensor;
        this.calcCDyn = calcCDyn;
    }

    /** Recomputes the approach metrics from the current sensor reading. */
    public void compute() {
        this.relNsDist = sensor.nsRelDist(calcCDyn.cdyn());
        this.relEwDist = sensor.ewRelDist(calcCDyn.cdyn());
        this.relNsVel = sensor.obsNsVel(calcCDyn.cdyn()) - sensor.nsVel();
        this.relEwVel = sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ewVel();
        this.relSpeedSq = this.relNsVel * this.relNsVel + this.relEwVel * this.relEwVel
                + MIN_REL_SPEED_SQ;
        this.tcpa = -(this.relNsDist * this.relNsVel + this.relEwDist * this.relEwVel)
                / this.relSpeedSq;
        this.cda = Math.sqrt(
                (this.relNsDist + this.relNsVel * this.tcpa)
                        * (this.relNsDist + this.relNsVel * this.tcpa)
                + (this.relEwDist + this.relEwVel * this.tcpa)
                        * (this.relEwDist + this.relEwVel * this.tcpa));
    }

    /** Closest Distance of Approach, in metres. */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }

    /** Time at Closest Point of Approach, in seconds. */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }
}
