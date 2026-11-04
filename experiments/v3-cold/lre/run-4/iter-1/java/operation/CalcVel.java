package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcVel (LRE-OP1): the AUV's horizontal, vertical and total velocity.
 */
public final class CalcVel {

    private final Sensor sensor;
    @RoboChartType("real") private double hvel = 0.0;
    @RoboChartType("real") private double vvel = 0.0;
    @RoboChartType("real") private double vel = 0.0;

    /** Creates the operation over the given sensor. */
    public CalcVel(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes hvel, vvel and vel from the current sensor reading. */
    public void compute() {
        this.hvel = Math.sqrt(sensor.nsVel() * sensor.nsVel() + sensor.ewVel() * sensor.ewVel());
        this.vvel = sensor.rateOfClimb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    /** Horizontal velocity magnitude, m/s (LRE-Var2). */
    @RoboChartType("real")
    public double hvel() {
        return hvel;
    }

    /** Vertical velocity (rate of climb), m/s (LRE-Var3). */
    @RoboChartType("real")
    public double vvel() {
        return vvel;
    }

    /** Overall velocity magnitude, m/s (LRE-Var4). */
    @RoboChartType("real")
    public double vel() {
        return vel;
    }
}
