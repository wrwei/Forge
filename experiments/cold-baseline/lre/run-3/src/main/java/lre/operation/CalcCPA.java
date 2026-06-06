package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * LRE-OP5: Closest Distance of Approach (cda) and Time at Closest Point of Approach (tcpa)
 * for the closest dynamic obstacle, identified by cdyn.
 *
 * The compute() body has only two field assignments. The sentinel case (cdyn == -1)
 * is handled by the Sensor layer, which returns zero-velocity and large-distance defaults.
 *
 * Standard CPA formulae:
 *   Let r = relative position of obstacle, v = relative velocity of obstacle.
 *   tcpa = - (r . v) / |v|^2
 *   cda  = |r + v * tcpa|
 * Since the AUV's horizontal velocity contribution is captured by the obstacle's relative
 * velocity vector (ns_vel/ew_vel observed relative to the AUV frame), we use the obstacle
 * velocity components directly.
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
        this.tcpa = -1.0;
    }

    public void compute() {
        this.tcpa = -(sensor.nsRelDist(calcCDyn.cdyn()) * sensor.obsNsVel(calcCDyn.cdyn()) + sensor.ewRelDist(calcCDyn.cdyn()) * sensor.obsEwVel(calcCDyn.cdyn())) / (sensor.obsNsVel(calcCDyn.cdyn()) * sensor.obsNsVel(calcCDyn.cdyn()) + sensor.obsEwVel(calcCDyn.cdyn()) * sensor.obsEwVel(calcCDyn.cdyn()) + 0.000001);
        this.cda = Math.sqrt((sensor.nsRelDist(calcCDyn.cdyn()) + sensor.obsNsVel(calcCDyn.cdyn()) * this.tcpa) * (sensor.nsRelDist(calcCDyn.cdyn()) + sensor.obsNsVel(calcCDyn.cdyn()) * this.tcpa) + (sensor.ewRelDist(calcCDyn.cdyn()) + sensor.obsEwVel(calcCDyn.cdyn()) * this.tcpa) * (sensor.ewRelDist(calcCDyn.cdyn()) + sensor.obsEwVel(calcCDyn.cdyn()) * this.tcpa));
    }

    @RoboChartType("real")
    public double cda() { return cda; }

    @RoboChartType("real")
    public double tcpa() { return tcpa; }
}
