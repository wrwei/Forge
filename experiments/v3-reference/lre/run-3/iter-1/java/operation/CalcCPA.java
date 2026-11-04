package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the closest distance of approach (cda) and the time at the closest
 * point of approach (tcpa) to the closest dynamic obstacle. The horizontal
 * approach uses the obstacle's position relative to the AUV and the velocity
 * of the AUV relative to the obstacle; the vertical separation is taken as
 * constant over the approach.
 */
public final class CalcCPA {
    private final Sensor sensor;
    @RoboChartType("nat") private int dynIndex;
    @RoboChartType("real") private double auvRelVelNs;
    @RoboChartType("real") private double auvRelVelEw;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cpaNsSep;
    @RoboChartType("real") private double cpaEwSep;
    @RoboChartType("real") private double cda;

    public CalcCPA(Sensor sensor) { this.sensor = sensor; }

    public void compute() {
        this.dynIndex = sensor.closestDynamicIndex();
        this.auvRelVelNs = sensor.nsVel() - sensor.obsNsVel(this.dynIndex);
        this.auvRelVelEw = sensor.ewVel() - sensor.obsEwVel(this.dynIndex);
        this.tcpa = (sensor.nsRelDist(this.dynIndex) * this.auvRelVelNs
                + sensor.ewRelDist(this.dynIndex) * this.auvRelVelEw)
                / (this.auvRelVelNs * this.auvRelVelNs + this.auvRelVelEw * this.auvRelVelEw
                        + LreConstants.CPA_EPSILON);
        this.cpaNsSep = sensor.nsRelDist(this.dynIndex) - this.auvRelVelNs * this.tcpa;
        this.cpaEwSep = sensor.ewRelDist(this.dynIndex) - this.auvRelVelEw * this.tcpa;
        this.cda = Math.sqrt(this.cpaNsSep * this.cpaNsSep + this.cpaEwSep * this.cpaEwSep
                + sensor.vdist(this.dynIndex) * sensor.vdist(this.dynIndex));
    }

    /** Closest distance of approach (metres). */
    @RoboChartType("real") public double cda() { return cda; }

    /** Time at closest point of approach (seconds). */
    @RoboChartType("real") public double tcpa() { return tcpa; }
}
