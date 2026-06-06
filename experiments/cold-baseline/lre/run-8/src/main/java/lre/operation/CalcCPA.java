package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

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
        this.cda = Math.sqrt(
                (sensor.nsRelDist(calcCDyn.cdyn()) + sensor.obsNsVel(calcCDyn.cdyn()) *
                        ((sensor.nsRelDist(calcCDyn.cdyn()) * (sensor.ns_vel() - sensor.obsNsVel(calcCDyn.cdyn())) +
                          sensor.ewRelDist(calcCDyn.cdyn()) * (sensor.ew_vel() - sensor.obsEwVel(calcCDyn.cdyn()))) /
                         ((sensor.ns_vel() - sensor.obsNsVel(calcCDyn.cdyn())) * (sensor.ns_vel() - sensor.obsNsVel(calcCDyn.cdyn())) +
                          (sensor.ew_vel() - sensor.obsEwVel(calcCDyn.cdyn())) * (sensor.ew_vel() - sensor.obsEwVel(calcCDyn.cdyn())) + 1.0e-9))
                  - sensor.nsRelDist(calcCDyn.cdyn()))
                  * (sensor.nsRelDist(calcCDyn.cdyn()) + sensor.obsNsVel(calcCDyn.cdyn()) *
                        ((sensor.nsRelDist(calcCDyn.cdyn()) * (sensor.ns_vel() - sensor.obsNsVel(calcCDyn.cdyn())) +
                          sensor.ewRelDist(calcCDyn.cdyn()) * (sensor.ew_vel() - sensor.obsEwVel(calcCDyn.cdyn()))) /
                         ((sensor.ns_vel() - sensor.obsNsVel(calcCDyn.cdyn())) * (sensor.ns_vel() - sensor.obsNsVel(calcCDyn.cdyn())) +
                          (sensor.ew_vel() - sensor.obsEwVel(calcCDyn.cdyn())) * (sensor.ew_vel() - sensor.obsEwVel(calcCDyn.cdyn())) + 1.0e-9))
                  - sensor.nsRelDist(calcCDyn.cdyn()))
                +
                (sensor.ewRelDist(calcCDyn.cdyn()) + sensor.obsEwVel(calcCDyn.cdyn()) *
                        ((sensor.nsRelDist(calcCDyn.cdyn()) * (sensor.ns_vel() - sensor.obsNsVel(calcCDyn.cdyn())) +
                          sensor.ewRelDist(calcCDyn.cdyn()) * (sensor.ew_vel() - sensor.obsEwVel(calcCDyn.cdyn()))) /
                         ((sensor.ns_vel() - sensor.obsNsVel(calcCDyn.cdyn())) * (sensor.ns_vel() - sensor.obsNsVel(calcCDyn.cdyn())) +
                          (sensor.ew_vel() - sensor.obsEwVel(calcCDyn.cdyn())) * (sensor.ew_vel() - sensor.obsEwVel(calcCDyn.cdyn())) + 1.0e-9))
                  - sensor.ewRelDist(calcCDyn.cdyn()))
                  * (sensor.ewRelDist(calcCDyn.cdyn()) + sensor.obsEwVel(calcCDyn.cdyn()) *
                        ((sensor.nsRelDist(calcCDyn.cdyn()) * (sensor.ns_vel() - sensor.obsNsVel(calcCDyn.cdyn())) +
                          sensor.ewRelDist(calcCDyn.cdyn()) * (sensor.ew_vel() - sensor.obsEwVel(calcCDyn.cdyn()))) /
                         ((sensor.ns_vel() - sensor.obsNsVel(calcCDyn.cdyn())) * (sensor.ns_vel() - sensor.obsNsVel(calcCDyn.cdyn())) +
                          (sensor.ew_vel() - sensor.obsEwVel(calcCDyn.cdyn())) * (sensor.ew_vel() - sensor.obsEwVel(calcCDyn.cdyn())) + 1.0e-9))
                  - sensor.ewRelDist(calcCDyn.cdyn())));
        this.tcpa = (sensor.nsRelDist(calcCDyn.cdyn()) * (sensor.ns_vel() - sensor.obsNsVel(calcCDyn.cdyn())) +
                    sensor.ewRelDist(calcCDyn.cdyn()) * (sensor.ew_vel() - sensor.obsEwVel(calcCDyn.cdyn()))) /
                   ((sensor.ns_vel() - sensor.obsNsVel(calcCDyn.cdyn())) * (sensor.ns_vel() - sensor.obsNsVel(calcCDyn.cdyn())) +
                    (sensor.ew_vel() - sensor.obsEwVel(calcCDyn.cdyn())) * (sensor.ew_vel() - sensor.obsEwVel(calcCDyn.cdyn())) + 1.0e-9);
    }

    @RoboChartType("real")
    public double cda() { return cda; }

    @RoboChartType("real")
    public double tcpa() { return tcpa; }
}
