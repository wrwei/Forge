package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach and the Time at Closest Point of
 * Approach to the closest dynamic obstacle (LRE-OP5).
 *
 * <p>The horizontal approach uses the AUV's velocity relative to the obstacle;
 * the vertical separation (vdist) is taken as constant over the approach and
 * is included in cda. When there is no dynamic obstacle, the sensor's zero
 * defaults and safe large vdist make cda large.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("nat") private int dynamicIndex;
    @RoboChartType("real") private double auvRelNsVel;
    @RoboChartType("real") private double auvRelEwVel;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cpaNsDist;
    @RoboChartType("real") private double cpaEwDist;
    @RoboChartType("real") private double cda;

    /** Creates the operation over the given sensor. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes cda and tcpa for the closest dynamic obstacle (the one CalcCDyn selects). */
    public void compute() {
        this.dynamicIndex = sensor.closestDynamicIndex();
        this.auvRelNsVel = sensor.nsVel() - sensor.obsNsVel(this.dynamicIndex);
        this.auvRelEwVel = sensor.ewVel() - sensor.obsEwVel(this.dynamicIndex);
        this.tcpa = (sensor.nsRelDist(this.dynamicIndex) * this.auvRelNsVel
                + sensor.ewRelDist(this.dynamicIndex) * this.auvRelEwVel)
                / (this.auvRelNsVel * this.auvRelNsVel + this.auvRelEwVel * this.auvRelEwVel);
        this.cpaNsDist = sensor.nsRelDist(this.dynamicIndex) - this.auvRelNsVel * this.tcpa;
        this.cpaEwDist = sensor.ewRelDist(this.dynamicIndex) - this.auvRelEwVel * this.tcpa;
        this.cda = Math.sqrt(this.cpaNsDist * this.cpaNsDist + this.cpaEwDist * this.cpaEwDist
                + sensor.vdist(this.dynamicIndex) * sensor.vdist(this.dynamicIndex));
    }

    /** Closest Distance of Approach (m). */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }

    /** Time at Closest Point of Approach (s); negative when the closest point has passed. */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }
}
