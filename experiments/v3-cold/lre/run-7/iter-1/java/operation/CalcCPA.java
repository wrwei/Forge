package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcCPA: closest distance of approach (cda) and time at closest point of approach (tcpa)
 * to the closest dynamic obstacle (the index CalcCDyn selects), assuming constant velocities.
 * The horizontal approach uses the AUV's velocity relative to the obstacle; cda is taken at
 * max(tcpa, 0), so once the closest point has passed it is the current separation, and it adds
 * the current vertical separation, so with no dynamic obstacle (zero position and velocity,
 * large vdist from the sensor) tcpa is 0 and cda is the sensor's safe large distance.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("nat") private int dynIndex;
    @RoboChartType("real") private double relNsVel;
    @RoboChartType("real") private double relEwVel;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double tcpaAhead;
    @RoboChartType("real") private double cda;

    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa and cda; the 0.000001 term keeps the division defined at zero relative velocity. */
    public void compute() {
        this.dynIndex = sensor.closestDynamicIndex();
        this.relNsVel = sensor.nsVel() - sensor.obsNsVel(this.dynIndex);
        this.relEwVel = sensor.ewVel() - sensor.obsEwVel(this.dynIndex);
        this.tcpa = (sensor.nsRelDist(this.dynIndex) * this.relNsVel
                + sensor.ewRelDist(this.dynIndex) * this.relEwVel)
                / (this.relNsVel * this.relNsVel + this.relEwVel * this.relEwVel + 0.000001);
        this.tcpaAhead = (this.tcpa + Math.sqrt(this.tcpa * this.tcpa)) / 2.0;
        this.cda = Math.sqrt(
                (sensor.nsRelDist(this.dynIndex) - this.relNsVel * this.tcpaAhead)
                        * (sensor.nsRelDist(this.dynIndex) - this.relNsVel * this.tcpaAhead)
                + (sensor.ewRelDist(this.dynIndex) - this.relEwVel * this.tcpaAhead)
                        * (sensor.ewRelDist(this.dynIndex) - this.relEwVel * this.tcpaAhead)
                + sensor.vdist(this.dynIndex) * sensor.vdist(this.dynIndex));
    }

    /** Closest distance of approach (m). */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }

    /** Time at closest point of approach (s); negative when the obstacle is moving away. */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }
}
