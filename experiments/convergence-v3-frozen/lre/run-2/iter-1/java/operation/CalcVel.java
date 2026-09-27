package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * LRE-OP1: derives the AUV's horizontal, vertical and overall velocity from the
 * raw sensor readings. Invoked once per step before the transition guards.
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
        this.hvel = Math.sqrt(sensor.ns_vel() * sensor.ns_vel()
                + sensor.ew_vel() * sensor.ew_vel());
        this.vvel = sensor.rate_of_climb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    /** LRE-Var2: horizontal velocity, m/s. */
    @RoboChartType("real")
    public double hvel() {
        return hvel;
    }

    /** LRE-Var3: vertical velocity, m/s. */
    @RoboChartType("real")
    public double vvel() {
        return vvel;
    }

    /** LRE-Var4: overall velocity magnitude, m/s. */
    @RoboChartType("real")
    public double vel() {
        return vel;
    }
}
