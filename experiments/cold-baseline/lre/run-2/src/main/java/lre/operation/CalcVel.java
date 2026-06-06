package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcVel (LRE-OP1) — computes horizontal, vertical, and total velocity
 * magnitudes from raw sensor inputs.
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

    public void compute() {
        this.hvel = Math.sqrt(sensor.nsVel() * sensor.nsVel() + sensor.ewVel() * sensor.ewVel());
        this.vvel = sensor.rateOfClimb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    @RoboChartType("real")
    public double hvel() {
        return hvel;
    }

    @RoboChartType("real")
    public double vvel() {
        return vvel;
    }

    @RoboChartType("real")
    public double vel() {
        return vel;
    }
}
