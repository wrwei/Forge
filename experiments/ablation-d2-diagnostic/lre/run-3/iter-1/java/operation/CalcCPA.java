package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and the Time at Closest Point
 * of Approach (tcpa) to the closest dynamic obstacle.
 */
public final class CalcCPA {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cdyn;

    @RoboChartType("real")
    private double relNsVel;

    @RoboChartType("real")
    private double relEwVel;

    @RoboChartType("real")
    private double closingRate;

    @RoboChartType("real")
    private double projNsDist;

    @RoboChartType("real")
    private double projEwDist;

    @RoboChartType("real")
    private double cda;

    @RoboChartType("real")
    private double tcpa;

    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
        this.relNsVel = sensor.relNsVel(this.cdyn);
        this.relEwVel = sensor.relEwVel(this.cdyn);
        this.closingRate = (sensor.nsRelDist(this.cdyn) * this.relNsVel
                + sensor.ewRelDist(this.cdyn) * this.relEwVel) * -1.0;
        this.tcpa = this.closingRate / sensor.relSpeedSq(this.cdyn);
        this.projNsDist = sensor.nsRelDist(this.cdyn) + this.relNsVel * this.tcpa;
        this.projEwDist = sensor.ewRelDist(this.cdyn) + this.relEwVel * this.tcpa;
        this.cda = Math.sqrt(this.projNsDist * this.projNsDist + this.projEwDist * this.projEwDist);
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
