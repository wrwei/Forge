package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the horizontal, vertical and total velocity of the AUV.
 */
public final class CalcVel {
    private final Sensor sensor;
    @RoboChartType("real") private double hvel;
    @RoboChartType("real") private double vvel;
    @RoboChartType("real") private double vel;

    public CalcVel(Sensor sensor) { this.sensor = sensor; }

    public void compute() {
        this.hvel = Math.sqrt(sensor.nsVel() * sensor.nsVel() + sensor.ewVel() * sensor.ewVel());
        this.vvel = sensor.rateOfClimb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    /** Horizontal velocity (m/s). */
    @RoboChartType("real") public double hvel() { return hvel; }

    /** Vertical velocity (m/s). */
    @RoboChartType("real") public double vvel() { return vvel; }

    /** Total velocity (m/s). */
    @RoboChartType("real") public double vel() { return vel; }
}
