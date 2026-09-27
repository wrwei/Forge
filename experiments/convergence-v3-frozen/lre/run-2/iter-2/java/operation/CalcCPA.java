package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * LRE-OP5: computes the Closest Distance of Approach (cda) and the Time at
 * Closest Point of Approach (tcpa) to the closest dynamic obstacle, from that
 * obstacle's relative position and relative velocity. Invoked once per step
 * before the transition guards.
 */
public final class CalcCPA {

    private final Sensor sensor;

    private final CalcCDyn calcCDyn;

    @RoboChartType("real")
    private double relNsVel;

    @RoboChartType("real")
    private double relEwVel;

    @RoboChartType("real")
    private double tcpa;

    @RoboChartType("real")
    private double cpaNsOffset;

    @RoboChartType("real")
    private double cpaEwOffset;

    @RoboChartType("real")
    private double cda;

    public CalcCPA(Sensor sensor, CalcCDyn calcCDyn) {
        this.sensor = sensor;
        this.calcCDyn = calcCDyn;
    }

    public void compute() {
        this.relNsVel = sensor.obsNsVel(calcCDyn.cdyn()) - sensor.ns_vel();
        this.relEwVel = sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ew_vel();
        this.tcpa = -(sensor.nsRelDist(calcCDyn.cdyn()) * this.relNsVel
                + sensor.ewRelDist(calcCDyn.cdyn()) * this.relEwVel)
                / sensor.relSpeedSq(calcCDyn.cdyn());
        this.cpaNsOffset = sensor.nsRelDist(calcCDyn.cdyn()) + this.relNsVel * this.tcpa;
        this.cpaEwOffset = sensor.ewRelDist(calcCDyn.cdyn()) + this.relEwVel * this.tcpa;
        this.cda = Math.sqrt(this.cpaNsOffset * this.cpaNsOffset
                + this.cpaEwOffset * this.cpaEwOffset);
    }

    /** LRE-Var7: closest distance of approach, metres. */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }

    /** LRE-Var8: time at closest point of approach, seconds. */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }
}
