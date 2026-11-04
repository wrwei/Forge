package lre.operation;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Closest distance of approach and time at closest point of approach to the
 * closest dynamic obstacle (LRE-OP5).
 *
 * <p>The obstacle's relative position p (nsRelDist, ewRelDist) changes at
 * -r, where r is the AUV's horizontal velocity relative to the obstacle. Then
 * tcpa = (p . r) / |r|^2 and the horizontal miss vector is p - r * tcpa. The
 * vertical separation (vdist) is taken as constant, so cda is the overall
 * distance at closest approach. A missing obstacle gives p = 0, tcpa = 0 and
 * cda = vdist, the sensor's safe large distance.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("nat") private int dynIndex;
    @RoboChartType("real") private double relNsVel;
    @RoboChartType("real") private double relEwVel;
    @RoboChartType("real") private double approach;
    @RoboChartType("real") private double relSpeedSq;
    @RoboChartType("real") private double missNs;
    @RoboChartType("real") private double missEw;
    @RoboChartType("real") private double cda;
    @RoboChartType("real") private double tcpa;

    /** An operation reading {@code sensor}. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cda and tcpa from the sensor. */
    public void compute() {
        this.dynIndex = sensor.closestDynamicIndex();
        this.relNsVel = sensor.nsVel() - sensor.obsNsVel(this.dynIndex);
        this.relEwVel = sensor.ewVel() - sensor.obsEwVel(this.dynIndex);
        this.approach = sensor.nsRelDist(this.dynIndex) * this.relNsVel
                + sensor.ewRelDist(this.dynIndex) * this.relEwVel;
        this.relSpeedSq = this.relNsVel * this.relNsVel + this.relEwVel * this.relEwVel;
        this.tcpa = this.approach / (this.relSpeedSq + LreConstants.CPA_SPEED_EPS);
        this.missNs = sensor.nsRelDist(this.dynIndex) - this.relNsVel * this.tcpa;
        this.missEw = sensor.ewRelDist(this.dynIndex) - this.relEwVel * this.tcpa;
        this.cda = Math.sqrt(this.missNs * this.missNs + this.missEw * this.missEw
                + sensor.vdist(this.dynIndex) * sensor.vdist(this.dynIndex));
    }

    /** Closest distance of approach, m (LRE-Var7). */
    @RoboChartType("real") public double cda() { return cda; }

    /** Time at closest point of approach, s (LRE-Var8). */
    @RoboChartType("real") public double tcpa() { return tcpa; }
}
