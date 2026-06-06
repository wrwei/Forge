package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcCPA (LRE-OP5). Computes the Closest Distance of Approach (cda) and
 * Time at Closest Point of Approach (tcpa) to the closest dynamic obstacle (cdyn).
 *
 * Sentinel handling (cdyn == -1) is delegated to the Sensor layer, which returns
 * zero-velocity and large-distance defaults when no dynamic obstacle exists.
 *
 * The compute() body contains only two field assignments (cda and tcpa).
 * Standard CPA formulae for a 2D horizontal encounter:
 *   relative position vector r = (ns_rel_dist, ew_rel_dist)
 *   relative velocity vector v = (-obs_ns_vel, -obs_ew_vel)   (own velocity is the frame)
 *   tcpa = -(r . v) / (v . v)
 *   cda  = sqrt( |r|^2 - (r . v)^2 / (v . v) )
 * We inline the dot products as single expressions to comply with the
 * "no intermediate fields/locals" rule.
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
        this.tcpa = -(sensor.nsRelDist(calcCDyn.cdyn()) * (-sensor.obsNsVel(calcCDyn.cdyn())) + sensor.ewRelDist(calcCDyn.cdyn()) * (-sensor.obsEwVel(calcCDyn.cdyn()))) / (sensor.obsNsVel(calcCDyn.cdyn()) * sensor.obsNsVel(calcCDyn.cdyn()) + sensor.obsEwVel(calcCDyn.cdyn()) * sensor.obsEwVel(calcCDyn.cdyn()) + 1.0e-9);
        this.cda = Math.sqrt(Math.max(0.0, sensor.nsRelDist(calcCDyn.cdyn()) * sensor.nsRelDist(calcCDyn.cdyn()) + sensor.ewRelDist(calcCDyn.cdyn()) * sensor.ewRelDist(calcCDyn.cdyn()) - (sensor.nsRelDist(calcCDyn.cdyn()) * (-sensor.obsNsVel(calcCDyn.cdyn())) + sensor.ewRelDist(calcCDyn.cdyn()) * (-sensor.obsEwVel(calcCDyn.cdyn()))) * (sensor.nsRelDist(calcCDyn.cdyn()) * (-sensor.obsNsVel(calcCDyn.cdyn())) + sensor.ewRelDist(calcCDyn.cdyn()) * (-sensor.obsEwVel(calcCDyn.cdyn()))) / (sensor.obsNsVel(calcCDyn.cdyn()) * sensor.obsNsVel(calcCDyn.cdyn()) + sensor.obsEwVel(calcCDyn.cdyn()) * sensor.obsEwVel(calcCDyn.cdyn()) + 1.0e-9)));
    }

    @RoboChartType("real")
    public double cda() { return cda; }

    @RoboChartType("real")
    public double tcpa() { return tcpa; }
}
