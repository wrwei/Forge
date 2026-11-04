package lre.operation;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and the Time at Closest
 * Point of Approach (tcpa) to the closest dynamic obstacle, in the horizontal
 * plane (LRE-OP5; variables LRE-Var7, LRE-Var8).
 *
 * <p>With p the obstacle's position relative to the AUV (ns_rel_dist,
 * ew_rel_dist) and c the closing velocity (AUV velocity minus obstacle
 * velocity), tcpa = (p.c) / (|c|^2 + CPA_EPSILON) and
 * cda = sqrt(hdist^2 - tcpa * (p.c)). The obstacle is the one at
 * {@code sensor.closestDynamicIndex()}, the index CalcCDyn reads from the same
 * sensor. With no dynamic obstacle the sensor gives zero position and velocity
 * and a safe large hdist, so tcpa = 0 and cda is that large distance.
 */
public final class CalcCPA {

    private final Sensor sensor;

    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Updates tcpa, then cda from tcpa. */
    public void compute() {
        this.tcpa = (sensor.nsRelDist(sensor.closestDynamicIndex())
                        * (sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                    + sensor.ewRelDist(sensor.closestDynamicIndex())
                        * (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex())))
                / ((sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                        * (sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                    + (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex()))
                        * (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex()))
                    + LreConstants.CPA_EPSILON);
        this.cda = Math.sqrt(sensor.hdist(sensor.closestDynamicIndex()) * sensor.hdist(sensor.closestDynamicIndex())
                - this.tcpa * (sensor.nsRelDist(sensor.closestDynamicIndex())
                        * (sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                    + sensor.ewRelDist(sensor.closestDynamicIndex())
                        * (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex()))));
    }

    /** Time at closest point of approach (s); negative when the closest point is past. */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }

    /** Closest distance of approach (m). */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }
}
