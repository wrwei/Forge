package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcVel: the AUV's horizontal velocity (hvel), vertical velocity (vvel) and
 * total velocity (vel), in m/s, from the raw sensor velocities.
 */
public final class CalcVel {

    private final Sensor sensor;
    @RoboChartType("real") private double hvel;
    @RoboChartType("real") private double vvel;
    @RoboChartType("real") private double vel;

    public CalcVel(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes hvel, vvel and vel from the current sensor reading. */
    public void compute() {
        this.hvel = Math.sqrt(sensor.nsVel() * sensor.nsVel() + sensor.ewVel() * sensor.ewVel());
        this.vvel = sensor.rateOfClimb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    /** Horizontal velocity magnitude, in m/s. */
    @RoboChartType("real")
    public double hvel() {
        return hvel;
    }

    /** Vertical velocity (the rate of climb), in m/s. */
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
