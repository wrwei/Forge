package lre.operation;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Closest distance of approach and time at closest point of approach to the
 * closest dynamic obstacle (LRE-OP5). The approach is computed in the
 * horizontal plane from the obstacle's relative position and the closing
 * velocity (AUV velocity minus obstacle velocity); the vertical separation is
 * taken as constant and added to the miss distance. With no dynamic obstacle
 * the sensor's defaults (zero position and velocity, large vertical distance)
 * make cda large.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("nat") private int dynObsIndex;
    @RoboChartType("real") private double relNsDist;
    @RoboChartType("real") private double relEwDist;
    @RoboChartType("real") private double closingNsVel;
    @RoboChartType("real") private double closingEwVel;
    @RoboChartType("real") private double vertSep;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double missNsDist;
    @RoboChartType("real") private double missEwDist;
    @RoboChartType("real") private double cda;

    /** An operation reading the closest dynamic obstacle through {@code sensor}. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa and cda from the sensor. */
    public void compute() {
        this.dynObsIndex = sensor.closestDynamicIndex();
        this.relNsDist = sensor.nsRelDist(this.dynObsIndex);
        this.relEwDist = sensor.ewRelDist(this.dynObsIndex);
        this.closingNsVel = sensor.nsVel() - sensor.obsNsVel(this.dynObsIndex);
        this.closingEwVel = sensor.ewVel() - sensor.obsEwVel(this.dynObsIndex);
        this.vertSep = sensor.vdist(this.dynObsIndex);
        this.tcpa = (this.relNsDist * this.closingNsVel + this.relEwDist * this.closingEwVel)
                / (this.closingNsVel * this.closingNsVel + this.closingEwVel * this.closingEwVel
                        + LreConstants.CPA_SPEED_SQ_EPS);
        this.missNsDist = this.relNsDist - this.closingNsVel * this.tcpa;
        this.missEwDist = this.relEwDist - this.closingEwVel * this.tcpa;
        this.cda = Math.sqrt(this.missNsDist * this.missNsDist + this.missEwDist * this.missEwDist
                + this.vertSep * this.vertSep);
    }

    /** Closest distance of approach, metres (LRE-Var7). */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }

    /** Time at closest point of approach, seconds (LRE-Var8). */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }
}
