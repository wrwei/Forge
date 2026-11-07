package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcVel (LRE-OP1): the AUV's horizontal, vertical and total velocity from
 * the raw sensor readings (LRE-Var2, LRE-Var3, LRE-Var4).
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

    /** Recomputes hvel, vvel and vel from the current sensor readings. */
    public void compute() {
        this.hvel = Math.sqrt(sensor.nsVel() * sensor.nsVel() + sensor.ewVel() * sensor.ewVel());
        this.vvel = sensor.rateOfClimb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    /** Horizontal velocity magnitude, m/s. */
    @RoboChartType("real")
    public double hvel() {
        return hvel;
    }

    /** Vertical velocity (rate of climb), m/s. */
    @RoboChartType("real")
    public double vvel() {
        return vvel;
    }

    /** Total velocity magnitude, m/s. */
    @RoboChartType("real")
    public double vel() {
        return vel;
    }
}
