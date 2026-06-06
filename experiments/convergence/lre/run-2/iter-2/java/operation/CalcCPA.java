package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and the Time at
 * Closest Point of Approach (tcpa) to the closest dynamic obstacle
 * (LRE-OP5), from the obstacle's relative position and velocity in the
 * horizontal plane.
 *
 * <p>The squared current separation is taken from hdist, so the Sensor
 * layer's large-distance default makes cda safely large when no dynamic
 * obstacle exists. When the relative velocity is zero, the separation
 * never changes: cda is the current distance and tcpa is -1 (never
 * approaching). Invoked by the controller before evaluating transition
 * guards each step.
 */
public final class CalcCPA {

    private final Sensor sensor;

    private int cdynIndex = -1;

    @RoboChartType("real")
    private double relNsVel;

    @RoboChartType("real")
    private double relEwVel;

    @RoboChartType("real")
    private double closingSq;

    @RoboChartType("real")
    private double approachDot;

    @RoboChartType("real")
    private double cdaSq;

    @RoboChartType("real")
    private double cda;

    @RoboChartType("real")
    private double tcpa;

    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.cdynIndex = sensor.closestDynamicIndex();
        this.relNsVel = sensor.obsNsVel(this.cdynIndex) - sensor.nsVel();
        this.relEwVel = sensor.obsEwVel(this.cdynIndex) - sensor.ewVel();
        this.closingSq = this.relNsVel * this.relNsVel + this.relEwVel * this.relEwVel;
        this.approachDot = sensor.nsRelDist(this.cdynIndex) * this.relNsVel
                + sensor.ewRelDist(this.cdynIndex) * this.relEwVel;
        if (this.closingSq > 0.0) {
            this.tcpa = -this.approachDot / this.closingSq;
            this.cdaSq = sensor.hdist(this.cdynIndex) * sensor.hdist(this.cdynIndex)
                    - this.approachDot * this.approachDot / this.closingSq;
        } else {
            this.tcpa = -1.0;
            this.cdaSq = sensor.hdist(this.cdynIndex) * sensor.hdist(this.cdynIndex);
        }
        if (this.cdaSq > 0.0) {
            this.cda = Math.sqrt(this.cdaSq);
        } else {
            this.cda = 0.0;
        }
    }

    public double cda() {
        return cda;
    }

    public double tcpa() {
        return tcpa;
    }
}
