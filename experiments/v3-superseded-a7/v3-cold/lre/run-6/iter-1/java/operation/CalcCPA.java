package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcCPA: the time at closest point of approach (tcpa, seconds) and the
 * closest distance of approach (cda, metres) to the closest dynamic obstacle.
 *
 * <p>With r the obstacle's horizontal position relative to the AUV and
 * w = (AUV velocity - obstacle velocity), the obstacle's relative position is
 * r - w t, so tcpa = (r . w) / |w|^2 and the horizontal miss distance is
 * |r - w tcpa|. The vertical separation (vdist) is taken as constant over the
 * approach, so cda = sqrt(|r - w tcpa|^2 + vdist^2). The term 0.000001 in the
 * denominator keeps the division defined when the relative velocity is zero,
 * which then gives tcpa = 0 and cda = the current distance. With no dynamic
 * obstacle the sensor reports zero position and velocity and a large vdist,
 * so tcpa = 0 and cda is large.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa and then cda for the closest dynamic obstacle. */
    public void compute() {
        this.tcpa = (sensor.nsRelDist(sensor.closestDynamicIndex())
                        * (sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                    + sensor.ewRelDist(sensor.closestDynamicIndex())
                        * (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex())))
                / ((sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                        * (sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                    + (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex()))
                        * (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex()))
                    + 0.000001);
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

    /** Time at closest point of approach, in seconds (negative if the approach is past). */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }

    /** Closest distance of approach, in metres. */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }
}
