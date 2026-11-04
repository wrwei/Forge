package lre.operation;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Computes the closest distance of approach and the time at the closest point
 * of approach to the closest dynamic obstacle (LRE-OP5).
 *
 * <p>With r the obstacle's horizontal position relative to the AUV and w the
 * AUV's horizontal velocity relative to the obstacle (AUV velocity minus
 * obstacle velocity), the obstacle's relative position at time t is r - w t,
 * so tcpa = (r . w) / |w|^2 and the horizontal miss distance is |r - w tcpa|.
 * The vertical separation is taken as constant and added to give the overall
 * cda. CPA_EPSILON keeps tcpa defined when w is zero. With no dynamic obstacle
 * the sensor gives r = 0 and a safe large vertical distance, so tcpa = 0 and
 * cda is large.
 */
public final class CalcCPA {

    private final Sensor sensor;

    @RoboChartType("real") private double cda = 0.0;
    @RoboChartType("real") private double tcpa = 0.0;

    /** Creates the operation over the AUV's sensor. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa and cda for the closest dynamic obstacle (as CalcCDyn selects it). */
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
        this.cda = Math.sqrt(
                (sensor.nsRelDist(sensor.closestDynamicIndex())
                        - (sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex())) * this.tcpa)
                    * (sensor.nsRelDist(sensor.closestDynamicIndex())
                        - (sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex())) * this.tcpa)
                + (sensor.ewRelDist(sensor.closestDynamicIndex())
                        - (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex())) * this.tcpa)
                    * (sensor.ewRelDist(sensor.closestDynamicIndex())
                        - (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex())) * this.tcpa)
                + sensor.vdist(sensor.closestDynamicIndex()) * sensor.vdist(sensor.closestDynamicIndex()));
    }

    /** Closest distance of approach, metres (LRE-Var7). */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }

    /** Time at the closest point of approach, seconds; negative once the obstacle has passed (LRE-Var8). */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }
}
