package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * LRE-OP5: computes the Closest Distance of Approach (cda) and the
 * Time at Closest Point of Approach (tcpa) for the closest dynamic
 * obstacle identified by cdyn.
 *
 * <p>Per the requirement, the compute() body contains ONLY two field
 * assignments. Intermediate quantities (relative positions, relative
 * velocities) are inlined as nested sensor accessor calls.
 *
 * <p>Formulae (standard CPA equations):
 * <pre>
 *   tcpa = -(dn*vn + de*ve) / (vn*vn + ve*ve)
 *   cda  = sqrt((dn + vn*tcpa)^2 + (de + ve*tcpa)^2)
 * </pre>
 * where dn = nsRelDist(cdyn), de = ewRelDist(cdyn),
 *       vn = obsNsVel(cdyn),  ve = obsEwVel(cdyn).
 *
 * <p>The cdyn == -1 sentinel is handled by the Sensor layer: when no
 * dynamic obstacle exists, accessors return 0.0, yielding NaN for
 * tcpa and cda. Java comparisons against NaN are all false, so
 * downstream guards (tcpa >= 0, cda < minSafeDist) cannot fire.
 */
public final class CalcCPA {

    private final Sensor sensor;
    private final CalcCDyn calcCDyn;

    @RoboChartType("real")
    private double tcpa;

    @RoboChartType("real")
    private double cda;

    public CalcCPA(Sensor sensor, CalcCDyn calcCDyn) {
        this.sensor = sensor;
        this.calcCDyn = calcCDyn;
    }

    public void compute() {
        this.tcpa = -(sensor.nsRelDist(calcCDyn.cdyn()) * sensor.obsNsVel(calcCDyn.cdyn()) + sensor.ewRelDist(calcCDyn.cdyn()) * sensor.obsEwVel(calcCDyn.cdyn())) / (sensor.obsNsVel(calcCDyn.cdyn()) * sensor.obsNsVel(calcCDyn.cdyn()) + sensor.obsEwVel(calcCDyn.cdyn()) * sensor.obsEwVel(calcCDyn.cdyn()));
        this.cda = Math.sqrt((sensor.nsRelDist(calcCDyn.cdyn()) + sensor.obsNsVel(calcCDyn.cdyn()) * this.tcpa) * (sensor.nsRelDist(calcCDyn.cdyn()) + sensor.obsNsVel(calcCDyn.cdyn()) * this.tcpa) + (sensor.ewRelDist(calcCDyn.cdyn()) + sensor.obsEwVel(calcCDyn.cdyn()) * this.tcpa) * (sensor.ewRelDist(calcCDyn.cdyn()) + sensor.obsEwVel(calcCDyn.cdyn()) * this.tcpa));
    }

    @RoboChartType("real")
    public double tcpa() { return tcpa; }

    @RoboChartType("real")
    public double cda() { return cda; }
}
