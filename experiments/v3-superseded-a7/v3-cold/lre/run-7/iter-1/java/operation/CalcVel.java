package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the AUV's horizontal, vertical and total velocity from the raw
 * sensor readings (LRE-OP1; variables LRE-Var2..Var4).
 */
public final class CalcVel {

    private final Sensor sensor;

    @RoboChartType("real") private double hvel;
    @RoboChartType("real") private double vvel;
    @RoboChartType("real") private double vel;

    /** Creates the operation over the given sensor. */
    public CalcVel(Sensor sensor) {
        this.sensor = sensor;
    }

    /** hvel = |(ns_vel, ew_vel)|, vvel = rate of climb, vel = |(hvel, vvel)|. */
    public void compute() {
        this.hvel = Math.sqrt(sensor.nsVel() * sensor.nsVel() + sensor.ewVel() * sensor.ewVel());
        this.vvel = sensor.rateOfClimb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    /** Horizontal velocity, in m/s. */
    @RoboChartType("real")
    public double hvel() {
        return hvel;
    }

    /** Vertical velocity, in m/s. */
    @RoboChartType("real")
    public double vvel() {
        return vvel;
    }

    /** Overall velocity magnitude, in m/s. */
    @RoboChartType("real")
    public double vel() {
        return vel;
    }
}
