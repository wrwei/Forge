package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the AUV's horizontal, vertical and overall velocity from the raw
 * sensor readings (LRE-OP1).
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

    /** Recomputes hvel, vvel and vel for the current cycle. */
    public void compute() {
        this.hvel = Math.sqrt(sensor.nsVel() * sensor.nsVel() + sensor.ewVel() * sensor.ewVel());
        this.vvel = sensor.rateOfClimb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    /** Horizontal velocity, in m/s (LRE-Var2). */
    @RoboChartType("real")
    public double hvel() {
        return hvel;
    }

    /** Vertical velocity, in m/s (LRE-Var3). */
    @RoboChartType("real")
    public double vvel() {
        return vvel;
    }

    /** Overall velocity magnitude, in m/s (LRE-Var4). */
    @RoboChartType("real")
    public double vel() {
        return vel;
    }
}
