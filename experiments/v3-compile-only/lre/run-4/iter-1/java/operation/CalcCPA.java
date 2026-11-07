package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and the Time at Closest
 * Point of Approach (tcpa) to the closest dynamic obstacle (LRE-OP5), in the
 * horizontal plane, from the obstacle's relative position r and its velocity
 * relative to the AUV v (obstacle velocity minus AUV velocity):
 *
 * <pre>
 *   tcpa = -(r . v) / (|v|^2 + CPA_EPSILON)
 *   cda  = |r + v * tcpa| + (hdist(cdyn) - |r|)
 * </pre>
 *
 * <p>The closest dynamic obstacle is the one CalcCDyn selects: both read
 * {@code sensor.closestDynamicIndex()} on the same sensor state.
 * CPA_EPSILON keeps the division defined when v is zero. The last term of cda
 * is exactly zero when the obstacle exists (the sensor computes hdist as |r|)
 * and is the sensor's safe large distance when there is no dynamic obstacle,
 * whose position and velocity the sensor reports as zero; cda is then large
 * and no collision is reported.
 */
public final class CalcCPA {

    private final Sensor sensor;

    @RoboChartType("real") private double tcpa = 0.0;
    @RoboChartType("real") private double cda = 0.0;

    /** Creates the operation over the given sensor. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa and cda from the current sensor readings. */
    public void compute() {
        this.tcpa = -(sensor.nsRelDist(sensor.closestDynamicIndex())
                        * (sensor.obsNsVel(sensor.closestDynamicIndex()) - sensor.nsVel())
                + sensor.ewRelDist(sensor.closestDynamicIndex())
                        * (sensor.obsEwVel(sensor.closestDynamicIndex()) - sensor.ewVel()))
                / ((sensor.obsNsVel(sensor.closestDynamicIndex()) - sensor.nsVel())
                        * (sensor.obsNsVel(sensor.closestDynamicIndex()) - sensor.nsVel())
                + (sensor.obsEwVel(sensor.closestDynamicIndex()) - sensor.ewVel())
                        * (sensor.obsEwVel(sensor.closestDynamicIndex()) - sensor.ewVel())
                + LreConstants.CPA_EPSILON);
        this.cda = Math.sqrt(
                (sensor.nsRelDist(sensor.closestDynamicIndex())
                        + (sensor.obsNsVel(sensor.closestDynamicIndex()) - sensor.nsVel()) * this.tcpa)
                * (sensor.nsRelDist(sensor.closestDynamicIndex())
                        + (sensor.obsNsVel(sensor.closestDynamicIndex()) - sensor.nsVel()) * this.tcpa)
                + (sensor.ewRelDist(sensor.closestDynamicIndex())
                        + (sensor.obsEwVel(sensor.closestDynamicIndex()) - sensor.ewVel()) * this.tcpa)
                * (sensor.ewRelDist(sensor.closestDynamicIndex())
                        + (sensor.obsEwVel(sensor.closestDynamicIndex()) - sensor.ewVel()) * this.tcpa))
                + (sensor.hdist(sensor.closestDynamicIndex())
                        - Math.sqrt(sensor.nsRelDist(sensor.closestDynamicIndex())
                                * sensor.nsRelDist(sensor.closestDynamicIndex())
                        + sensor.ewRelDist(sensor.closestDynamicIndex())
                                * sensor.ewRelDist(sensor.closestDynamicIndex())));
    }

    /** Time at closest point of approach in seconds; negative if the CPA is past (LRE-Var8). */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }

    /** Closest distance of approach in metres (LRE-Var7). */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }
}
