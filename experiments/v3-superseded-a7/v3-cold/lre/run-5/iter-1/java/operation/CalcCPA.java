package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and the Time at Closest Point
 * of Approach (tcpa) to the closest dynamic obstacle (LRE-OP5), in the horizontal
 * plane.
 *
 * <p>With p the obstacle's position relative to the AUV and w the AUV's velocity
 * relative to the obstacle (the closing velocity), tcpa = (p.w) / (|w|^2 + e) and
 * cda^2 = |p|^2 - tcpa^2 (|w|^2 + e), where e is {@link LreConstants#CPA_EPSILON}.
 * |p| is taken from sensor.hdist, so that with no dynamic obstacle (p = 0, |p| a
 * safe large distance) tcpa is 0 and cda is that large distance.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("real") private double closingNsVel;
    @RoboChartType("real") private double closingEwVel;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Recomputes cda and tcpa. The closest dynamic obstacle is the one CalcCDyn
     * selects (sensor.closestDynamicIndex()).
     */
    public void compute() {
        this.closingNsVel = sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex());
        this.closingEwVel = sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex());
        this.tcpa = (sensor.nsRelDist(sensor.closestDynamicIndex()) * this.closingNsVel
                + sensor.ewRelDist(sensor.closestDynamicIndex()) * this.closingEwVel)
                / (this.closingNsVel * this.closingNsVel + this.closingEwVel * this.closingEwVel
                + LreConstants.CPA_EPSILON);
        this.cda = Math.sqrt(sensor.hdist(sensor.closestDynamicIndex()) * sensor.hdist(sensor.closestDynamicIndex())
                - this.tcpa * this.tcpa * (this.closingNsVel * this.closingNsVel
                + this.closingEwVel * this.closingEwVel + LreConstants.CPA_EPSILON));
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
