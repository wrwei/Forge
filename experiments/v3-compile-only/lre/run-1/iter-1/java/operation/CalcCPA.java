package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * LRE-OP5: Closest Distance of Approach (cda) and Time at Closest Point of
 * Approach (tcpa) to the closest dynamic obstacle (the index CalcCDyn selects,
 * sensor.closestDynamicIndex()).
 *
 * <p>With r = (nsRelDist, ewRelDist) the obstacle's position relative to the
 * AUV and w = (nsVel - obsNsVel, ewVel - obsEwVel) the AUV's velocity relative
 * to the obstacle, the obstacle's relative position at time t is r - w*t, so
 * <pre>
 *   tcpa = (r . w) / (|w|^2 + cpaEpsilon)
 *   cda  = sqrt(|r - w*tcpa|^2 + vdist^2)
 * </pre>
 * cpaEpsilon keeps the division defined when the relative velocity is zero.
 * The vertical separation (vdist) is held constant over the approach; it also
 * carries the sensor's safe large distance into cda when there is no dynamic
 * obstacle, where the field accessors return zero.
 */
public final class CalcCPA {

    private final Sensor sensor;

    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    /** The operation over the given sensor. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa and then cda from the sensor. */
    public void compute() {
        this.tcpa = (sensor.nsRelDist(sensor.closestDynamicIndex())
                        * (sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                    + sensor.ewRelDist(sensor.closestDynamicIndex())
                        * (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex())))
                / ((sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                        * (sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                    + (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex()))
                        * (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex()))
                    + LreConstants.cpaEpsilon);
        this.cda = Math.sqrt(
                (sensor.nsRelDist(sensor.closestDynamicIndex())
                        - (sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex())) * this.tcpa)
                    * (sensor.nsRelDist(sensor.closestDynamicIndex())
                        - (sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex())) * this.tcpa)
                + (sensor.ewRelDist(sensor.closestDynamicIndex())
                        - (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex())) * this.tcpa)
                    * (sensor.ewRelDist(sensor.closestDynamicIndex())
                        - (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex())) * this.tcpa)
                + sensor.vdist(sensor.closestDynamicIndex()) * sensor.vdist(sensor.closestDynamicIndex()));
    }

    /** LRE-Var8: time at closest point of approach (s); negative when the obstacle is receding. */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }

    /** LRE-Var7: closest distance of approach (m). */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }
}
