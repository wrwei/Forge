package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the closest distance of approach and the time at the closest point
 * of approach to the closest dynamic obstacle (LRE-OP5), in the horizontal
 * plane. With r the obstacle's relative position and c the closing velocity
 * (AUV velocity minus obstacle velocity): tcpa = (r.c) / |c|^2 and
 * cda = sqrt(|r|^2 - tcpa^2 |c|^2), with |r| = hdist(index). A small constant
 * in the denominator keeps tcpa defined when |c| = 0 (then tcpa = 0 and
 * cda = hdist). With no dynamic obstacle the sensor's defaults give tcpa = 0
 * and cda = the sensor's safe large distance.
 */
public final class CalcCPA {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int dynIdx;

    @RoboChartType("real")
    private double closingNsVel;

    @RoboChartType("real")
    private double closingEwVel;

    @RoboChartType("real")
    private double tcpa;

    @RoboChartType("real")
    private double cda;

    /**
     * @param sensor the AUV sensor service
     */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa and cda. */
    public void compute() {
        this.dynIdx = sensor.closestDynamicIndex();
        this.closingNsVel = sensor.nsVel() - sensor.obsNsVel(this.dynIdx);
        this.closingEwVel = sensor.ewVel() - sensor.obsEwVel(this.dynIdx);
        this.tcpa = (sensor.nsRelDist(this.dynIdx) * this.closingNsVel
                + sensor.ewRelDist(this.dynIdx) * this.closingEwVel)
                / (this.closingNsVel * this.closingNsVel + this.closingEwVel * this.closingEwVel + 0.000001);
        this.cda = Math.sqrt(sensor.hdist(this.dynIdx) * sensor.hdist(this.dynIdx)
                - this.tcpa * this.tcpa
                * (this.closingNsVel * this.closingNsVel + this.closingEwVel * this.closingEwVel));
    }

    /** @return time at the closest point of approach (s) */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }

    /** @return closest distance of approach (m) */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }
}
