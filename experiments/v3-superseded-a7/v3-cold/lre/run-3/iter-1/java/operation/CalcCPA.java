package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the closest point of approach to the closest dynamic obstacle
 * (LRE-OP5).
 *
 * <p>With p the obstacle's horizontal position relative to the AUV and w the
 * closing velocity (AUV velocity minus obstacle velocity), the obstacle is at
 * p - w*t after t seconds. tcpa = (p.w) / (|w|^2 + 0.000001), the small term
 * keeping the division defined when w is zero, and cda is the distance at
 * tcpa, with the current vertical separation (vdist) held constant. With no
 * dynamic obstacle the sensor returns p = 0 and vdist large, so cda is large.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("real") private double closingNsVel;
    @RoboChartType("real") private double closingEwVel;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa and cda for the closest dynamic obstacle. */
    public void compute() {
        this.closingNsVel = sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex());
        this.closingEwVel = sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex());
        this.tcpa = (sensor.nsRelDist(sensor.closestDynamicIndex()) * this.closingNsVel
                + sensor.ewRelDist(sensor.closestDynamicIndex()) * this.closingEwVel)
                / (this.closingNsVel * this.closingNsVel + this.closingEwVel * this.closingEwVel + 0.000001);
        this.cda = Math.sqrt(
                (sensor.nsRelDist(sensor.closestDynamicIndex()) - this.closingNsVel * this.tcpa)
                        * (sensor.nsRelDist(sensor.closestDynamicIndex()) - this.closingNsVel * this.tcpa)
                + (sensor.ewRelDist(sensor.closestDynamicIndex()) - this.closingEwVel * this.tcpa)
                        * (sensor.ewRelDist(sensor.closestDynamicIndex()) - this.closingEwVel * this.tcpa)
                + sensor.vdist(sensor.closestDynamicIndex()) * sensor.vdist(sensor.closestDynamicIndex()));
    }

    /** Closest Distance of Approach, in metres (LRE-Var7). */
    @RoboChartType("real") public double cda() { return cda; }

    /** Time at Closest Point of Approach, in seconds (LRE-Var8). */
    @RoboChartType("real") public double tcpa() { return tcpa; }
}
