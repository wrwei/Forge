package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes horizontal, vertical, and total velocity from raw sensor
 * inputs (LRE-OP1).
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

    /** Recomputes hvel, vvel, and vel from the current sensor readings. */
    public void compute() {
        this.hvel = Math.sqrt(sensor.nsVel() * sensor.nsVel() + sensor.ewVel() * sensor.ewVel());
        this.vvel = sensor.rateOfClimb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    /** Horizontal velocity magnitude, m/s. */
    public double hvel() {
        return hvel;
    }

    /** Vertical velocity, m/s. */
    public double vvel() {
        return vvel;
    }

    /** Overall velocity magnitude, m/s. */
    public double vel() {
        return vel;
    }
}
