package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach and the Time at Closest Point of
 * Approach to the closest dynamic obstacle (LRE-OP5, LRE-Var7, LRE-Var8).
 *
 * <p>The horizontal closest point of approach is projected from the relative
 * position and relative velocity of the obstacle; the vertical separation is
 * taken as it currently stands. When no dynamic obstacle exists the Sensor
 * reports zero relative position and velocity and a safe large vertical
 * distance, which yields a safe large cda; the Sensor also floors the squared
 * relative speed so the projection never divides by zero.
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
    private double projNsDist;

    @RoboChartType("real")
    private double projEwDist;

    @RoboChartType("real")
    private double cda;

    @RoboChartType("real")
    private double tcpa;

    public CalcCPA(Sensor sensor, CalcCDyn calcCDyn) {
        this.sensor = sensor;
        this.calcCDyn = calcCDyn;
    }

    /** Recomputes cda and tcpa for the closest dynamic obstacle. */
    public void compute() {
        this.cdyn = calcCDyn.cdyn();
        this.relNsVel = sensor.obsNsVel(this.cdyn) - sensor.nsVel();
        this.relEwVel = sensor.obsEwVel(this.cdyn) - sensor.ewVel();
        this.tcpa = (0.0 - (sensor.nsRelDist(this.cdyn) * this.relNsVel + sensor.ewRelDist(this.cdyn) * this.relEwVel)) / sensor.relSpeedSq(this.cdyn);
        this.projNsDist = sensor.nsRelDist(this.cdyn) + this.relNsVel * this.tcpa;
        this.projEwDist = sensor.ewRelDist(this.cdyn) + this.relEwVel * this.tcpa;
        this.cda = Math.sqrt(this.projNsDist * this.projNsDist + this.projEwDist * this.projEwDist + sensor.vdist(this.cdyn) * sensor.vdist(this.cdyn));
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
