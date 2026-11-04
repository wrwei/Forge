package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * The CalcCPA operation (LRE-OP5): Closest Distance of Approach (cda) and Time
 * to Closest Point of Approach (tcpa) to the closest dynamic obstacle, the one
 * CalcCDyn selects, read from the same sensor function.
 *
 * <p>With p the obstacle's horizontal position relative to the AUV and w the
 * AUV's horizontal velocity relative to the obstacle, the horizontal
 * separation is least at tcpa = (p.w) / |w|^2. Assuming the vertical
 * separation stays as it is, the overall distance there is
 * cda = sqrt(odist^2 - tcpa^2 |w|^2). CPA_EPSILON keeps |w|^2 positive, so that
 * with no relative motion tcpa is 0 and cda is the current distance. Using
 * odist makes the sensor's large-distance default the result when there is no
 * dynamic obstacle.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("nat") private int cpaTarget;
    @RoboChartType("real") private double relVelNs;
    @RoboChartType("real") private double relVelEw;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    /** CalcCPA over the given sensor. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Computes tcpa and cda. */
    public void compute() {
        this.cpaTarget = sensor.closestDynamicIndex();
        this.relVelNs = sensor.nsVel() - sensor.obsNsVel(this.cpaTarget);
        this.relVelEw = sensor.ewVel() - sensor.obsEwVel(this.cpaTarget);
        this.tcpa = (sensor.nsRelDist(this.cpaTarget) * this.relVelNs + sensor.ewRelDist(this.cpaTarget) * this.relVelEw)
                / (this.relVelNs * this.relVelNs + this.relVelEw * this.relVelEw + LreConstants.CPA_EPSILON);
        this.cda = Math.sqrt(sensor.odist(this.cpaTarget) * sensor.odist(this.cpaTarget)
                - this.tcpa * this.tcpa * (this.relVelNs * this.relVelNs + this.relVelEw * this.relVelEw + LreConstants.CPA_EPSILON));
    }

    /** Closest Distance of Approach, metres (LRE-Var7). */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }

    /** Time to the Closest Point of Approach, seconds (LRE-Var8). */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }
}
