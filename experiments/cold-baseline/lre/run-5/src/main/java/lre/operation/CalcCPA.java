package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * LRE-OP5: cda = closest distance of approach to cdyn;
 *          tcpa = time at closest point of approach to cdyn.
 *
 * Standard CPA formulas, computed in two field assignments. Sensor accessors
 * return safe zeros when cdyn == -1, so no sentinel guard is needed here.
 *
 * Let dx = nsRelDist(cdyn), dy = ewRelDist(cdyn),
 *     vx = obsNsVel(cdyn)  - sensor.ns_vel(),
 *     vy = obsEwVel(cdyn)  - sensor.ew_vel().
 * tcpa = -(dx*vx + dy*vy) / (vx*vx + vy*vy + 1.0e-9)
 * cda  = sqrt((dx + vx*tcpa)^2 + (dy + vy*tcpa)^2)
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
        this.tcpa = -(sensor.nsRelDist(calcCDyn.cdyn()) * (sensor.obsNsVel(calcCDyn.cdyn()) - sensor.ns_vel())
                    + sensor.ewRelDist(calcCDyn.cdyn()) * (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ew_vel()))
                    / ((sensor.obsNsVel(calcCDyn.cdyn()) - sensor.ns_vel()) * (sensor.obsNsVel(calcCDyn.cdyn()) - sensor.ns_vel())
                     + (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ew_vel()) * (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ew_vel())
                     + 1.0e-9);
        this.cda = Math.sqrt(
                (sensor.nsRelDist(calcCDyn.cdyn()) + (sensor.obsNsVel(calcCDyn.cdyn()) - sensor.ns_vel()) * this.tcpa)
              * (sensor.nsRelDist(calcCDyn.cdyn()) + (sensor.obsNsVel(calcCDyn.cdyn()) - sensor.ns_vel()) * this.tcpa)
              + (sensor.ewRelDist(calcCDyn.cdyn()) + (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ew_vel()) * this.tcpa)
              * (sensor.ewRelDist(calcCDyn.cdyn()) + (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ew_vel()) * this.tcpa));
    }

    @RoboChartType("real")
    public double cda() { return cda; }

    @RoboChartType("real")
    public double tcpa() { return tcpa; }
}
