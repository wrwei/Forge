package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the closest distance of approach cda (m) and the time to the
 * closest point of approach tcpa (s) to the closest dynamic obstacle, the one
 * CalcCDyn selects, read through the same Sensor selection function.
 * Horizontal motion is extrapolated at constant relative velocity; the current
 * vertical separation is kept. With no dynamic obstacle the Sensor's defaults
 * (zero relative position and velocity, large vertical distance) give tcpa = 0
 * and a large cda.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("real") private double cpaNsDist;
    @RoboChartType("real") private double cpaEwDist;
    @RoboChartType("real") private double closingNsVel;
    @RoboChartType("real") private double closingEwVel;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Refreshes tcpa and cda from the current sensor reading. */
    public void compute() {
        this.cpaNsDist = sensor.nsRelDist(sensor.closestDynamicIndex());
        this.cpaEwDist = sensor.ewRelDist(sensor.closestDynamicIndex());
        this.closingNsVel = sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex());
        this.closingEwVel = sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex());
        this.tcpa = (this.cpaNsDist * this.closingNsVel + this.cpaEwDist * this.closingEwVel)
                / (this.closingNsVel * this.closingNsVel + this.closingEwVel * this.closingEwVel
                        + LreConstants.MIN_REL_SPEED_SQ);
        this.cda = Math.sqrt((this.cpaNsDist - this.closingNsVel * this.tcpa) * (this.cpaNsDist - this.closingNsVel * this.tcpa)
                + (this.cpaEwDist - this.closingEwVel * this.tcpa) * (this.cpaEwDist - this.closingEwVel * this.tcpa)
                + sensor.vdist(sensor.closestDynamicIndex()) * sensor.vdist(sensor.closestDynamicIndex()));
    }

    /** Time to the closest point of approach (s); negative if the obstacle is receding. */
    @RoboChartType("real") public double tcpa() {
        return tcpa;
    }

    /** Closest distance of approach (m). */
    @RoboChartType("real") public double cda() {
        return cda;
    }
}
