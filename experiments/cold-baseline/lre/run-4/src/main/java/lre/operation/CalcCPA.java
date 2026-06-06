package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Closest Distance of Approach (cda) and Time at Closest Point of Approach
 * (tcpa) to the closest dynamic obstacle identified by cdyn. See LRE-OP5.
 *
 * Per spec, the compute() body contains exactly two field assignments
 * (cda and tcpa) — no intermediate fields. The sentinel case (cdyn == -1) is
 * handled by the Sensor layer, which returns zero-velocity and large-distance
 * defaults when no dynamic obstacle exists.
 *
 * Standard derivation:
 *   relative speed^2 = (obsNsVel - ns_vel)^2 + (obsEwVel - ew_vel)^2
 *   tcpa = -(ns_rel_dist * (obsNsVel - ns_vel) + ew_rel_dist * (obsEwVel - ew_vel)) / relSpeed^2
 *   cda  = sqrt(odist^2 - relSpeed^2 * tcpa^2)
 * Inlined directly per the "only two assignments" rule.
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
        this.cda = Double.MAX_VALUE;
        this.tcpa = 0.0;
    }

    public void compute() {
        this.tcpa = -(sensor.nsRelDist(calcCDyn.cdyn()) * (sensor.obsNsVel(calcCDyn.cdyn()) - sensor.ns_vel()) + sensor.ewRelDist(calcCDyn.cdyn()) * (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ew_vel())) / ((sensor.obsNsVel(calcCDyn.cdyn()) - sensor.ns_vel()) * (sensor.obsNsVel(calcCDyn.cdyn()) - sensor.ns_vel()) + (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ew_vel()) * (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ew_vel()) + 1.0E-9);
        this.cda = Math.sqrt(sensor.odist(calcCDyn.cdyn()) * sensor.odist(calcCDyn.cdyn()) - ((sensor.obsNsVel(calcCDyn.cdyn()) - sensor.ns_vel()) * (sensor.obsNsVel(calcCDyn.cdyn()) - sensor.ns_vel()) + (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ew_vel()) * (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ew_vel())) * this.tcpa * this.tcpa);
    }

    @RoboChartType("real")
    public double cda() { return cda; }

    @RoboChartType("real")
    public double tcpa() { return tcpa; }
}
