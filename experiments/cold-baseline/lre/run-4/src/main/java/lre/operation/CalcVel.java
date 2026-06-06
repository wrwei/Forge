package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes horizontal velocity, vertical velocity, and total velocity.
 * See LRE-OP1.
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
        this.hvel = 0.0;
        this.vvel = 0.0;
        this.vel = 0.0;
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
