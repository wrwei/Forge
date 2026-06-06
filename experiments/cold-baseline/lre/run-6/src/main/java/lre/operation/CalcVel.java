package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * LRE-OP1: computes horizontal velocity (hvel), vertical velocity
 * (vvel), and overall velocity magnitude (vel) from the sensor.
 *
 * <ul>
 *   <li>hvel = sqrt(ns_vel^2 + ew_vel^2)</li>
 *   <li>vvel = rate_of_climb</li>
 *   <li>vel  = sqrt(hvel^2 + vvel^2)</li>
 * </ul>
 */
public final class CalcVel {

    private final Sensor sensor;

    @RoboChartType("real")
    private double hvel;

    @RoboChartType("real")
    private double vvel;

    @RoboChartType("real")
    private double vel;

    public CalcVel(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.hvel = Math.sqrt(sensor.ns_vel() * sensor.ns_vel() + sensor.ew_vel() * sensor.ew_vel());
        this.vvel = sensor.rate_of_climb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    @RoboChartType("real")
    public double hvel() { return hvel; }

    @RoboChartType("real")
    public double vvel() { return vvel; }

    @RoboChartType("real")
    public double vel() { return vel; }
}
