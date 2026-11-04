package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the closest distance of approach (cda) and the time to the closest
 * point of approach (tcpa) to the closest dynamic obstacle (LRE-OP5; variables
 * LRE-Var7, LRE-Var8).
 *
 * <p>The motion is horizontal: p is the obstacle's relative position
 * (ns_rel_dist, ew_rel_dist), v its velocity relative to the AUV, and the
 * vertical separation vdist is taken as constant. Then tcpa = -(p.v)/|v|^2
 * (a tiny constant keeps the divisor non-zero, so tcpa is 0 with no relative
 * motion), and cda is the overall distance at max(tcpa, 0), the closest
 * approach from now on. With no dynamic obstacle the sensor's defaults (zero
 * position and velocity, large vdist) give tcpa = 0 and a large cda.
 */
public final class CalcCPA {

    private final Sensor sensor;

    @RoboChartType("real") private double relNsVel;
    @RoboChartType("real") private double relEwVel;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double tcpaAhead;
    @RoboChartType("real") private double cpaNsDist;
    @RoboChartType("real") private double cpaEwDist;
    @RoboChartType("real") private double cda;

    /** Creates the operation over the given sensor. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Computes tcpa and cda for the obstacle CalcCDyn selects (same sensor function). */
    public void compute() {
        this.relNsVel = sensor.obsNsVel(sensor.closestDynamicIndex()) - sensor.nsVel();
        this.relEwVel = sensor.obsEwVel(sensor.closestDynamicIndex()) - sensor.ewVel();
        this.tcpa = (0.0 - (sensor.nsRelDist(sensor.closestDynamicIndex()) * this.relNsVel
                + sensor.ewRelDist(sensor.closestDynamicIndex()) * this.relEwVel))
                / (this.relNsVel * this.relNsVel + this.relEwVel * this.relEwVel + 0.000000001);
        this.tcpaAhead = (this.tcpa + Math.sqrt(this.tcpa * this.tcpa)) / 2.0;
        this.cpaNsDist = sensor.nsRelDist(sensor.closestDynamicIndex()) + this.relNsVel * this.tcpaAhead;
        this.cpaEwDist = sensor.ewRelDist(sensor.closestDynamicIndex()) + this.relEwVel * this.tcpaAhead;
        this.cda = Math.sqrt(this.cpaNsDist * this.cpaNsDist + this.cpaEwDist * this.cpaEwDist
                + sensor.vdist(sensor.closestDynamicIndex()) * sensor.vdist(sensor.closestDynamicIndex()));
    }

    /** Time to the closest point of approach, in seconds (negative if the obstacle recedes). */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }

    /** Closest distance of approach from now on, in metres. */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }
}
