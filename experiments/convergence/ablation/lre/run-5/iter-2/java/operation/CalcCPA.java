package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and Time at Closest
 * Point of Approach (tcpa) to the closest dynamic obstacle. The
 * no-dynamic-obstacle case is handled by the Sensor layer, which returns
 * large-distance and never-approaching defaults. Invoked by the
 * controller before evaluating transition guards each step.
 */
public final class CalcCPA {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cdynIdx = -1;

    @RoboChartType("real")
    private double cda;

    @RoboChartType("real")
    private double tcpa;

    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.cdynIdx = sensor.closestDynamicIndex();
        this.cda = sensor.cdaTo(this.cdynIdx);
        this.tcpa = sensor.tcpaTo(this.cdynIdx);
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
