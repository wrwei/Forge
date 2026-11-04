package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the closest distance of approach (cda) and the time at the closest
 * point of approach (tcpa) to the closest dynamic obstacle, in the horizontal
 * plane (LRE-OP5).
 *
 * <p>With r the obstacle's position relative to the AUV and w the AUV's
 * velocity relative to the obstacle, tcpa = (r.w) / |w|^2 and
 * cda^2 = |r|^2 - tcpa^2 |w|^2. |r| is taken from the sensor's hdist, so when
 * there is no dynamic obstacle (r and the obstacle velocity read as zero) cda
 * is the sensor's safe large distance and tcpa is zero.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("nat") private int dynIndex;
    @RoboChartType("real") private double relNs;
    @RoboChartType("real") private double relEw;
    @RoboChartType("real") private double closingNs;
    @RoboChartType("real") private double closingEw;
    @RoboChartType("real") private double closingSpeedSq;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    /** An operation reading from {@code sensor}. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa and cda from the current sensor readings. */
    public void compute() {
        this.dynIndex = sensor.closestDynamicIndex();
        this.relNs = sensor.nsRelDist(this.dynIndex);
        this.relEw = sensor.ewRelDist(this.dynIndex);
        this.closingNs = sensor.nsVel() - sensor.obsNsVel(this.dynIndex);
        this.closingEw = sensor.ewVel() - sensor.obsEwVel(this.dynIndex);
        this.closingSpeedSq = this.closingNs * this.closingNs + this.closingEw * this.closingEw
                + LreConstants.CPA_EPSILON;
        this.tcpa = (this.relNs * this.closingNs + this.relEw * this.closingEw) / this.closingSpeedSq;
        this.cda = Math.sqrt(sensor.hdist(this.dynIndex) * sensor.hdist(this.dynIndex)
                - this.tcpa * this.tcpa * this.closingSpeedSq);
    }

    /** Closest distance of approach, metres (LRE-Var7). */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }

    /** Time at the closest point of approach, seconds; negative if it has passed (LRE-Var8). */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }
}
