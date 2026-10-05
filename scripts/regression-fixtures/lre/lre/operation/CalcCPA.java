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
    }

    public void compute() {
        this.tcpa = -(sensor.nsRelDist(calcCDyn.cdyn()) * (sensor.obsNsVel(calcCDyn.cdyn()) - sensor.nsVel()) + sensor.ewRelDist(calcCDyn.cdyn()) * (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ewVel())) / ((sensor.obsNsVel(calcCDyn.cdyn()) - sensor.nsVel()) * (sensor.obsNsVel(calcCDyn.cdyn()) - sensor.nsVel()) + (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ewVel()) * (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ewVel()));
        this.cda = Math.sqrt((sensor.nsRelDist(calcCDyn.cdyn()) + this.tcpa * (sensor.obsNsVel(calcCDyn.cdyn()) - sensor.nsVel())) * (sensor.nsRelDist(calcCDyn.cdyn()) + this.tcpa * (sensor.obsNsVel(calcCDyn.cdyn()) - sensor.nsVel())) + (sensor.ewRelDist(calcCDyn.cdyn()) + this.tcpa * (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ewVel())) * (sensor.ewRelDist(calcCDyn.cdyn()) + this.tcpa * (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ewVel())));
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
