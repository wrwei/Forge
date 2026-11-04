package lre.operation;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Computes the closest distance of approach and the time to the closest point
 * of approach to the closest dynamic obstacle (LRE-OP5).
 *
 * <p>With p the obstacle's horizontal position relative to the AUV and w the
 * AUV's horizontal velocity relative to the obstacle (AUV velocity minus
 * obstacle velocity): tcpa = (p . w) / (|w|^2 + CPA_EPSILON), and cda is the
 * distance at tcpa, sqrt(|p - w * tcpa|^2 + vdist^2), with the vertical
 * separation taken as constant over the encounter. With no dynamic obstacle
 * the sensor returns zero position and velocity and a safe large vdist, so
 * tcpa is 0 and cda is that large distance.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("real") private double cda = 0.0;
    @RoboChartType("real") private double tcpa = 0.0;

    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa, then cda, for the obstacle CalcCDyn selects (sensor.closestDynamicIndex()). */
    public void compute() {
        this.tcpa = (sensor.nsRelDist(sensor.closestDynamicIndex())
                        * (sensor.ns_vel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                    + sensor.ewRelDist(sensor.closestDynamicIndex())
                        * (sensor.ew_vel() - sensor.obsEwVel(sensor.closestDynamicIndex())))
                / ((sensor.ns_vel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                        * (sensor.ns_vel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                    + (sensor.ew_vel() - sensor.obsEwVel(sensor.closestDynamicIndex()))
                        * (sensor.ew_vel() - sensor.obsEwVel(sensor.closestDynamicIndex()))
                    + LreConstants.CPA_EPSILON);
        this.cda = Math.sqrt(
                (sensor.nsRelDist(sensor.closestDynamicIndex())
                        - (sensor.ns_vel() - sensor.obsNsVel(sensor.closestDynamicIndex())) * this.tcpa)
                    * (sensor.nsRelDist(sensor.closestDynamicIndex())
                        - (sensor.ns_vel() - sensor.obsNsVel(sensor.closestDynamicIndex())) * this.tcpa)
                + (sensor.ewRelDist(sensor.closestDynamicIndex())
                        - (sensor.ew_vel() - sensor.obsEwVel(sensor.closestDynamicIndex())) * this.tcpa)
                    * (sensor.ewRelDist(sensor.closestDynamicIndex())
                        - (sensor.ew_vel() - sensor.obsEwVel(sensor.closestDynamicIndex())) * this.tcpa)
                + sensor.vdist(sensor.closestDynamicIndex()) * sensor.vdist(sensor.closestDynamicIndex()));
    }

    /** Closest Distance of Approach, metres (LRE-Var7). */
    @RoboChartType("real") public double cda() {
        return cda;
    }

    /** Time to the Closest Point of Approach, seconds (LRE-Var8). */
    @RoboChartType("real") public double tcpa() {
        return tcpa;
    }
}
