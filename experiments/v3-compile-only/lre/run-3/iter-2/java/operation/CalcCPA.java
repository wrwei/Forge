package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcCPA (LRE-OP5): Closest Distance of Approach (cda, LRE-Var7) and Time at
 * Closest Point of Approach (tcpa, LRE-Var8) to the closest dynamic obstacle,
 * the one CalcCDyn selects, sensor.closestDynamicIndex(). The approach is
 * computed in the horizontal plane from the obstacle's relative position and
 * the relative (closing) velocity, AUV velocity minus obstacle velocity.
 *
 * <p>With p the relative position, c the closing velocity and e a small
 * regularising term that keeps the denominator positive:
 * tcpa = (p . c) / (|c|^2 + e) and cda = sqrt(|p|^2 - tcpa^2 (|c|^2 + e)).
 * With no dynamic obstacle the sensor gives p = 0 and |p| (via hdist) a safe
 * large distance, so tcpa = 0 and cda is that large distance.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("real") private double closingNsVel;
    @RoboChartType("real") private double closingEwVel;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    /** Creates the operation over the given sensor. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa and cda from the current sensor readings. */
    public void compute() {
        this.closingNsVel = sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex());
        this.closingEwVel = sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex());
        this.tcpa = (sensor.nsRelDist(sensor.closestDynamicIndex()) * this.closingNsVel
                + sensor.ewRelDist(sensor.closestDynamicIndex()) * this.closingEwVel)
                / (this.closingNsVel * this.closingNsVel + this.closingEwVel * this.closingEwVel + 0.001);
        this.cda = Math.sqrt(sensor.hdist(sensor.closestDynamicIndex()) * sensor.hdist(sensor.closestDynamicIndex())
                - this.tcpa * this.tcpa
                * (this.closingNsVel * this.closingNsVel + this.closingEwVel * this.closingEwVel + 0.001));
    }

    /** Time at closest point of approach, seconds (negative if already past). */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }

    /** Closest distance of approach, metres. */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }
}
