package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * LRE-OP1: computes the AUV's horizontal velocity (hvel), vertical velocity
 * (vvel, the rate of climb) and overall velocity (vel) from the raw readings.
 */
public final class CalcVel {

    private final Sensor sensor;

    @RoboChartType("real") private double hvel;
    @RoboChartType("real") private double vvel;
    @RoboChartType("real") private double vel;

    /** The operation over the given sensor. */
    public CalcVel(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes hvel, vvel and vel from the current sensor readings. */
    public void compute() {
        this.hvel = Math.sqrt(sensor.nsVel() * sensor.nsVel() + sensor.ewVel() * sensor.ewVel());
        this.vvel = sensor.rateOfClimb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    /** LRE-Var2: horizontal velocity (m/s). */
    @RoboChartType("real")
    public double hvel() {
        return hvel;
    }

    /** LRE-Var3: vertical velocity (m/s). */
    @RoboChartType("real")
    public double vvel() {
        return vvel;
    }

    /** LRE-Var4: overall velocity magnitude (m/s). */
    @RoboChartType("real")
    public double vel() {
        return vel;
    }
}
