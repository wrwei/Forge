package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and Time at Closest Point
 * of Approach (tcpa) to the closest dynamic obstacle identified by cdyn
 * (LRE-OP5). Only two assignments: cda and tcpa. Sentinel case (cdyn == -1)
 * is handled by the Sensor layer.
 *
 * Standard CPA formulae (relative motion in horizontal plane):
 *   rel_speed_sq = (obs_ns_vel - 0)^2 + (obs_ew_vel - 0)^2     // AUV-relative
 *   tcpa = -(ns_rel_dist * obs_ns_vel + ew_rel_dist * obs_ew_vel) / rel_speed_sq
 *   cda  = sqrt( (ns_rel_dist + tcpa * obs_ns_vel)^2
 *              + (ew_rel_dist + tcpa * obs_ew_vel)^2 )
 *
 * Note: since the Sensor returns 0.0 for obs_ns_vel / obs_ew_vel when
 * cdyn == -1, the formulae yield cda = hdist(cdyn) = MAX_VALUE and a
 * harmless tcpa, which is the desired safe default.
 */
public final class CalcCPA {

    private final Sensor sensor;
    private final CalcCDyn calcCDyn;

    @RoboChartType("real")
    private double cda;

    @RoboChartType("real")
    private double tcpa;

    public CalcCPA(Sensor sensor, CalcCDyn calcCDyn) {
        this.sensor = sensor;
        this.calcCDyn = calcCDyn;
    }

    public void compute() {
        this.tcpa = -(sensor.nsRelDist(calcCDyn.cdyn()) * sensor.obsNsVel(calcCDyn.cdyn())
                + sensor.ewRelDist(calcCDyn.cdyn()) * sensor.obsEwVel(calcCDyn.cdyn()))
                / (sensor.obsNsVel(calcCDyn.cdyn()) * sensor.obsNsVel(calcCDyn.cdyn())
                        + sensor.obsEwVel(calcCDyn.cdyn()) * sensor.obsEwVel(calcCDyn.cdyn())
                        + 1.0e-9);
        this.cda = Math.sqrt(
                (sensor.nsRelDist(calcCDyn.cdyn()) + this.tcpa * sensor.obsNsVel(calcCDyn.cdyn()))
                        * (sensor.nsRelDist(calcCDyn.cdyn()) + this.tcpa * sensor.obsNsVel(calcCDyn.cdyn()))
                        + (sensor.ewRelDist(calcCDyn.cdyn()) + this.tcpa * sensor.obsEwVel(calcCDyn.cdyn()))
                                * (sensor.ewRelDist(calcCDyn.cdyn())
                                        + this.tcpa * sensor.obsEwVel(calcCDyn.cdyn())));
    }

    @RoboChartType("real")
    public double cda() { return cda; }

    @RoboChartType("real")
    public double tcpa() { return tcpa; }
}
