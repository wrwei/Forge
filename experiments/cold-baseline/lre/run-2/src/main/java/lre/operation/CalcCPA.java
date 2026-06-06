package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcCPA (LRE-OP5) — computes the Closest Distance of Approach (cda)
 * and Time at Closest Point of Approach (tcpa) to the closest dynamic
 * obstacle identified by {@code cdyn}.
 * <p>
 * The compute() method body contains only two field assignments. The
 * sentinel case (cdyn == -1) is handled by the Sensor layer, which
 * returns zero-velocity and large-distance defaults when no dynamic
 * obstacle exists.
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
        this.tcpa = -(sensor.nsRelDist(calcCDyn.cdyn()) * (sensor.nsVel() - sensor.obsNsVel(calcCDyn.cdyn()))
                + sensor.ewRelDist(calcCDyn.cdyn()) * (sensor.ewVel() - sensor.obsEwVel(calcCDyn.cdyn())))
                / ((sensor.nsVel() - sensor.obsNsVel(calcCDyn.cdyn())) * (sensor.nsVel() - sensor.obsNsVel(calcCDyn.cdyn()))
                        + (sensor.ewVel() - sensor.obsEwVel(calcCDyn.cdyn())) * (sensor.ewVel() - sensor.obsEwVel(calcCDyn.cdyn()))
                        + 1.0);
        this.cda = Math.sqrt(
                (sensor.nsRelDist(calcCDyn.cdyn()) + (sensor.nsVel() - sensor.obsNsVel(calcCDyn.cdyn())) * this.tcpa)
                        * (sensor.nsRelDist(calcCDyn.cdyn()) + (sensor.nsVel() - sensor.obsNsVel(calcCDyn.cdyn())) * this.tcpa)
                        + (sensor.ewRelDist(calcCDyn.cdyn()) + (sensor.ewVel() - sensor.obsEwVel(calcCDyn.cdyn())) * this.tcpa)
                                * (sensor.ewRelDist(calcCDyn.cdyn()) + (sensor.ewVel() - sensor.obsEwVel(calcCDyn.cdyn())) * this.tcpa));
    }

    @RoboChartType("real")
    public double cda() {
        return cda;
    }

    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }
}
