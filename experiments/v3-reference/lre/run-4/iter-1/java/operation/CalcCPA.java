package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the closest distance of approach (cda) and the time to the closest point of
 * approach (tcpa) to the closest dynamic obstacle (LRE-OP5).
 *
 * <p>The motion is horizontal: the obstacle's position relative to the AUV is p, the
 * closing velocity is w = AUV velocity - obstacle velocity, so the relative position at
 * time t is p - w t. Then tcpa = (p . w) / |w|^2 and cda is the distance at tcpa, with the
 * current vertical separation kept. With no dynamic obstacle the sensor gives p = 0 and a
 * large vertical distance, so cda is large.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("real") private double relNsDist;
    @RoboChartType("real") private double relEwDist;
    @RoboChartType("real") private double closingNsVel;
    @RoboChartType("real") private double closingEwVel;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    /** Creates the operation over the given sensor. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa and cda for the closest dynamic obstacle. */
    public void compute() {
        this.relNsDist = sensor.nsRelDist(sensor.closestDynamicIndex());
        this.relEwDist = sensor.ewRelDist(sensor.closestDynamicIndex());
        this.closingNsVel = sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex());
        this.closingEwVel = sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex());
        this.tcpa = (this.relNsDist * this.closingNsVel + this.relEwDist * this.closingEwVel)
                / (this.closingNsVel * this.closingNsVel + this.closingEwVel * this.closingEwVel
                        + LreConstants.CPA_EPSILON);
        this.cda = Math.sqrt(
                (this.relNsDist - this.closingNsVel * this.tcpa) * (this.relNsDist - this.closingNsVel * this.tcpa)
                + (this.relEwDist - this.closingEwVel * this.tcpa) * (this.relEwDist - this.closingEwVel * this.tcpa)
                + sensor.vdist(sensor.closestDynamicIndex()) * sensor.vdist(sensor.closestDynamicIndex()));
    }

    /** Time to the closest point of approach, in seconds (LRE-Var8). */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }

    /** Closest distance of approach, in metres (LRE-Var7). */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }
}
