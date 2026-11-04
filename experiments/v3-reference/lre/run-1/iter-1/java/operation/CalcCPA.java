package lre.operation;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * CalcCPA (LRE-OP5): time at, and distance of, the closest point of approach
 * to the closest dynamic obstacle (the one CalcCDyn selects).
 *
 * <p>Horizontal motion is extrapolated at constant relative velocity; the
 * vertical separation is taken as constant, so cda includes vdist. With no
 * dynamic obstacle the sensor returns zero relative position and velocity and
 * a safe large vdist, so cda is large and no collision is reported.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("nat") private int cpaIndex;
    @RoboChartType("real") private double closeNsVel;
    @RoboChartType("real") private double closeEwVel;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    /** An operation reading the given sensor. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa and cda from the current readings. */
    public void compute() {
        this.cpaIndex = sensor.closestDynamicIndex();
        this.closeNsVel = sensor.nsVel() - sensor.obsNsVel(this.cpaIndex);
        this.closeEwVel = sensor.ewVel() - sensor.obsEwVel(this.cpaIndex);
        this.tcpa = (sensor.nsRelDist(this.cpaIndex) * this.closeNsVel
                + sensor.ewRelDist(this.cpaIndex) * this.closeEwVel)
                / (this.closeNsVel * this.closeNsVel + this.closeEwVel * this.closeEwVel
                + LreConstants.MIN_REL_SPEED_SQ);
        this.cda = Math.sqrt(
                (sensor.nsRelDist(this.cpaIndex) - this.closeNsVel * this.tcpa)
                        * (sensor.nsRelDist(this.cpaIndex) - this.closeNsVel * this.tcpa)
                + (sensor.ewRelDist(this.cpaIndex) - this.closeEwVel * this.tcpa)
                        * (sensor.ewRelDist(this.cpaIndex) - this.closeEwVel * this.tcpa)
                + sensor.vdist(this.cpaIndex) * sensor.vdist(this.cpaIndex));
    }

    /** Time at closest point of approach, in seconds; negative when the obstacle is receding (LRE-Var8). */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }

    /** Closest distance of approach, in metres (LRE-Var7). */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }
}
