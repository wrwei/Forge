package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the AUV's horizontal, vertical and total velocity from the raw
 * sensor readings.
 */
public final class CalcVel {

    private final Sensor sensor;

    @RoboChartType("real")
    private double hvel = 0.0;

    @RoboChartType("real")
    private double vvel = 0.0;

    @RoboChartType("real")
    private double vel = 0.0;

    /**
     * Creates the operation over a sensor.
     *
     * @param sensor the AUV's sensor
     */
    public CalcVel(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes hvel, vvel and vel from the current sensor readings. */
    public void compute() {
        this.hvel = Math.sqrt(sensor.nsVel() * sensor.nsVel() + sensor.ewVel() * sensor.ewVel());
        this.vvel = sensor.rateOfClimb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    /**
     * Horizontal velocity: the magnitude of (ns_vel, ew_vel).
     *
     * @return hvel, m/s
     */
    @RoboChartType("real")
    public double hvel() {
        return hvel;
    }

    /**
     * Vertical velocity: the rate of climb.
     *
     * @return vvel, m/s
     */
    @RoboChartType("real")
    public double vvel() {
        return vvel;
    }

    /**
     * Total velocity: the magnitude of (hvel, vvel).
     *
     * @return vel, m/s
     */
    @RoboChartType("real")
    public double vel() {
        return vel;
    }
}
