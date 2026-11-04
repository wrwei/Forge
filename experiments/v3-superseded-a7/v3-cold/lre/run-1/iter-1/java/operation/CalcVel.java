package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the AUV's horizontal, vertical and total velocity from the raw
 * sensor inputs (LRE-OP1; variables LRE-Var2, LRE-Var3, LRE-Var4).
 */
public final class CalcVel {

    private final Sensor sensor;

    @RoboChartType("real") private double hvel;
    @RoboChartType("real") private double vvel;
    @RoboChartType("real") private double vel;

    public CalcVel(Sensor sensor) {
        this.sensor = sensor;
    }

    /** hvel = |(ns_vel, ew_vel)|, vvel = rate_of_climb, vel = |(hvel, vvel)|. */
    public void compute() {
        this.hvel = Math.sqrt(sensor.nsVel() * sensor.nsVel() + sensor.ewVel() * sensor.ewVel());
        this.vvel = sensor.rateOfClimb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    /** Horizontal velocity (m/s). */
    @RoboChartType("real")
    public double hvel() {
        return hvel;
    }

    /** Vertical velocity (m/s). */
    @RoboChartType("real")
    public double vvel() {
        return vvel;
    }

    /** Overall velocity magnitude (m/s). */
    @RoboChartType("real")
    public double vel() {
        return vel;
    }
}
