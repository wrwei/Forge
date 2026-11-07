package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the AUV's horizontal, vertical and total velocity from the raw
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

    /**
     * @param sensor the AUV sensor service
     */
    public CalcVel(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes hvel, vvel and vel. */
    public void compute() {
        this.hvel = Math.sqrt(sensor.nsVel() * sensor.nsVel() + sensor.ewVel() * sensor.ewVel());
        this.vvel = sensor.rateOfClimb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    /** @return horizontal velocity magnitude (m/s) */
    @RoboChartType("real")
    public double hvel() {
        return hvel;
    }

    /** @return vertical velocity (m/s) */
    @RoboChartType("real")
    public double vvel() {
        return vvel;
    }

    /** @return total velocity magnitude (m/s) */
    @RoboChartType("real")
    public double vel() {
        return vel;
    }
}
