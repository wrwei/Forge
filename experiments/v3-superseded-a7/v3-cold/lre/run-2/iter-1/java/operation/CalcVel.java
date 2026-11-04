package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcVel (LRE-OP1): horizontal, vertical and total velocity of the AUV.
 */
public final class CalcVel {

    private final Sensor sensor;
    @RoboChartType("real") private double hvel;
    @RoboChartType("real") private double vvel;
    @RoboChartType("real") private double vel;

    /** CalcVel reading the given sensor. */
    public CalcVel(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes hvel, vvel and vel from the sensor. */
    public void compute() {
        this.hvel = Math.sqrt(sensor.nsVel() * sensor.nsVel() + sensor.ewVel() * sensor.ewVel());
        this.vvel = sensor.rateOfClimb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    /** Horizontal velocity of the AUV, in m/s (LRE-Var2). */
    @RoboChartType("real")
    public double hvel() {
        return hvel;
    }

    /** Vertical velocity (rate of climb) of the AUV, in m/s (LRE-Var3). */
    @RoboChartType("real")
    public double vvel() {
        return vvel;
    }

    /** Overall velocity magnitude of the AUV, in m/s (LRE-Var4). */
    @RoboChartType("real")
    public double vel() {
        return vel;
    }
}
