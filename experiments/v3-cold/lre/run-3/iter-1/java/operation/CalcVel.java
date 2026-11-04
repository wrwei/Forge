package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/** Computes the AUV's horizontal, vertical and total velocity (m/s) from the raw sensor data. */
public final class CalcVel {

    private final Sensor sensor;
    @RoboChartType("real") private double hvel;
    @RoboChartType("real") private double vvel;
    @RoboChartType("real") private double vel;

    public CalcVel(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Refreshes hvel, vvel and vel from the current sensor reading. */
    public void compute() {
        this.hvel = Math.sqrt(sensor.nsVel() * sensor.nsVel() + sensor.ewVel() * sensor.ewVel());
        this.vvel = sensor.rateOfClimb();
        this.vel = Math.sqrt(this.hvel * this.hvel + this.vvel * this.vvel);
    }

    /** Magnitude of the horizontal velocity (m/s). */
    @RoboChartType("real") public double hvel() {
        return hvel;
    }

    /** Vertical velocity, the rate of climb (m/s). */
    @RoboChartType("real") public double vvel() {
        return vvel;
    }

    /** Magnitude of the overall velocity (m/s). */
    @RoboChartType("real") public double vel() {
        return vel;
    }
}
