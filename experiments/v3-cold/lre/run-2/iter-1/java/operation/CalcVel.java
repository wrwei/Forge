package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the AUV's horizontal, vertical and total velocity (LRE-OP1).
 */
public final class CalcVel {

    private final Sensor sensor;
    @RoboChartType("real") private double hvel = 0.0;
    @RoboChartType("real") private double vvel = 0.0;
    @RoboChartType("real") private double vel = 0.0;

    public CalcVel(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes hvel, vvel and vel from the current sensor readings. */
    public void compute() {
        this.hvel = Math.sqrt(sensor.ns_vel() * sensor.ns_vel() + sensor.ew_vel() * sensor.ew_vel());
        this.vvel = sensor.rate_of_climb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    /** Horizontal velocity, m/s (LRE-Var2). */
    @RoboChartType("real") public double hvel() {
        return hvel;
    }

    /** Vertical velocity, m/s (LRE-Var3). */
    @RoboChartType("real") public double vvel() {
        return vvel;
    }

    /** Overall velocity magnitude, m/s (LRE-Var4). */
    @RoboChartType("real") public double vel() {
        return vel;
    }
}
