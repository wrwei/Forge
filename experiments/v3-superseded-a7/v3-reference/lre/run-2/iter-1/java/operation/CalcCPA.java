package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcCPA (LRE-OP5): closest distance of approach (cda) and time at closest
 * point of approach (tcpa) to the closest dynamic obstacle.
 *
 * <p>The obstacle's relative position (ns_rel_dist, ew_rel_dist) is taken as
 * obstacle minus AUV, and the closing velocity as AUV velocity minus obstacle
 * velocity, so the relative position at time t is p - w t. tcpa is the time
 * that minimises its horizontal length (negative once the obstacle has
 * passed). cda is the distance at max(tcpa, 0), with the current vertical
 * separation added: with no dynamic obstacle that separation is the sensor's
 * safe large distance, so cda is large and no collision is reported.
 */
public final class CalcCPA {

    private final Sensor sensor;
    // The closest dynamic obstacle, read from the sensor as CalcCDyn reads it.
    @RoboChartType("nat") private int dynamicIndex;
    @RoboChartType("real") private double closingNsVel;
    @RoboChartType("real") private double closingEwVel;
    @RoboChartType("real") private double tcpa;
    // max(tcpa, 0): the closest approach still ahead.
    @RoboChartType("real") private double aheadTime;
    @RoboChartType("real") private double missNs;
    @RoboChartType("real") private double missEw;
    @RoboChartType("real") private double cda;

    /** Creates the operation over the given sensor. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cda and tcpa from the sensor. */
    public void compute() {
        this.dynamicIndex = sensor.closestDynamicIndex();
        this.closingNsVel = sensor.nsVel() - sensor.obsNsVel(this.dynamicIndex);
        this.closingEwVel = sensor.ewVel() - sensor.obsEwVel(this.dynamicIndex);
        this.tcpa = (sensor.nsRelDist(this.dynamicIndex) * this.closingNsVel
                + sensor.ewRelDist(this.dynamicIndex) * this.closingEwVel)
                / (this.closingNsVel * this.closingNsVel + this.closingEwVel * this.closingEwVel
                + LreConstants.CPA_EPSILON);
        this.aheadTime = (this.tcpa + Math.sqrt(this.tcpa * this.tcpa)) / 2.0;
        this.missNs = sensor.nsRelDist(this.dynamicIndex) - this.closingNsVel * this.aheadTime;
        this.missEw = sensor.ewRelDist(this.dynamicIndex) - this.closingEwVel * this.aheadTime;
        this.cda = Math.sqrt(this.missNs * this.missNs + this.missEw * this.missEw
                + sensor.vdist(this.dynamicIndex) * sensor.vdist(this.dynamicIndex));
    }

    /** Closest Distance of Approach, metres (LRE-Var7). */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }

    /** Time at Closest Point of Approach, seconds (LRE-Var8). */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }
}
