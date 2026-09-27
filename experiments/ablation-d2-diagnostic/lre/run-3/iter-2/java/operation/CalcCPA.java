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
    private double cda;

    @RoboChartType("real")
    private double tcpa;

    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
        this.tcpa = sensor.tcpaTo(this.cdyn);
        this.cda = sensor.cdaTo(this.cdyn);
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
