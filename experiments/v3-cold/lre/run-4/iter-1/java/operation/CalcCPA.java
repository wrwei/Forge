package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * CalcCPA (LRE-OP5): closest distance of approach (cda) and time at closest
 * point of approach (tcpa) to the closest dynamic obstacle, the one CalcCDyn
 * selects, {@code sensor.closestDynamicIndex()}.
 *
 * <p>Horizontal motion is linear: the obstacle's position relative to the
 * AUV is (nsRelDist, ewRelDist) and the AUV closes on it at the AUV velocity
 * minus the obstacle velocity. The vertical separation is taken as constant,
 * so cda combines the horizontal miss distance with vdist. With no dynamic
 * obstacle the sensor's defaults (zero offsets, large vdist) give a large cda.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("real") private double closingNsVel = 0.0;
    @RoboChartType("real") private double closingEwVel = 0.0;
    @RoboChartType("real") private double tcpa = 0.0;
    @RoboChartType("real") private double cda = 0.0;

    /** Creates the operation over the given sensor. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa and cda from the current sensor reading. */
    public void compute() {
        this.closingNsVel = sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex());
        this.closingEwVel = sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex());
        this.tcpa = (sensor.nsRelDist(sensor.closestDynamicIndex()) * this.closingNsVel
                + sensor.ewRelDist(sensor.closestDynamicIndex()) * this.closingEwVel)
                / (this.closingNsVel * this.closingNsVel + this.closingEwVel * this.closingEwVel);
        this.cda = Math.sqrt(
                (sensor.nsRelDist(sensor.closestDynamicIndex()) - this.closingNsVel * this.tcpa)
                        * (sensor.nsRelDist(sensor.closestDynamicIndex()) - this.closingNsVel * this.tcpa)
                + (sensor.ewRelDist(sensor.closestDynamicIndex()) - this.closingEwVel * this.tcpa)
                        * (sensor.ewRelDist(sensor.closestDynamicIndex()) - this.closingEwVel * this.tcpa)
                + sensor.vdist(sensor.closestDynamicIndex()) * sensor.vdist(sensor.closestDynamicIndex()));
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
