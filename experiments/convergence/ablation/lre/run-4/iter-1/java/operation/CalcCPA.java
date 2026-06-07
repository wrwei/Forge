package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and Time at Closest Point
 * of Approach (tcpa) to the closest dynamic obstacle (LRE-OP5). The
 * no-dynamic-obstacle case is handled by the Sensor layer, which returns
 * zero-velocity and large-distance defaults. Invoked by the controller
 * before transition guards are evaluated each step.
 */
public final class CalcCPA {

    private final Sensor sensor;

    @RoboChartType("real")
    private double cda;
    @RoboChartType("real")
    private double tcpa;

    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.cda = sensor.cpaDist(sensor.closestDynamicIndex());
        this.tcpa = sensor.cpaTime(sensor.closestDynamicIndex());
    }

    public double cda() {
        return cda;
    }

    public double tcpa() {
        return tcpa;
    }
}
