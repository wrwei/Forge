package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the time at closest point of approach (tcpa) and the closest
 * distance of approach (cda) to the closest dynamic obstacle (LRE-OP5).
 *
 * <p>The obstacle is the one CalcCDyn selects:
 * {@code sensor.closestDynamicIndex()}, read from the same sensor in the same
 * step. With relative position r = (nsRelDist, ewRelDist) and relative
 * velocity v = (obsNsVel, obsEwVel):
 * tcpa = -(r . v) / (|v|^2 + CPA_EPSILON) and cda = |r + v * tcpa|.
 * When there is no dynamic obstacle the sensor reports zero velocity and a
 * large distance, so tcpa = 0 and cda is large.
 */
public final class CalcCPA {

    private final Sensor sensor;

    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    /** Creates the operation over the given sensor. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa, then cda. */
    public void compute() {
        this.tcpa = (0.0 - (sensor.nsRelDist(sensor.closestDynamicIndex()) * sensor.obsNsVel(sensor.closestDynamicIndex())
                        + sensor.ewRelDist(sensor.closestDynamicIndex()) * sensor.obsEwVel(sensor.closestDynamicIndex())))
                / (sensor.obsNsVel(sensor.closestDynamicIndex()) * sensor.obsNsVel(sensor.closestDynamicIndex())
                        + sensor.obsEwVel(sensor.closestDynamicIndex()) * sensor.obsEwVel(sensor.closestDynamicIndex())
                        + LreConstants.CPA_EPSILON);
        this.cda = Math.sqrt(
                (sensor.nsRelDist(sensor.closestDynamicIndex()) + sensor.obsNsVel(sensor.closestDynamicIndex()) * this.tcpa)
                        * (sensor.nsRelDist(sensor.closestDynamicIndex()) + sensor.obsNsVel(sensor.closestDynamicIndex()) * this.tcpa)
                + (sensor.ewRelDist(sensor.closestDynamicIndex()) + sensor.obsEwVel(sensor.closestDynamicIndex()) * this.tcpa)
                        * (sensor.ewRelDist(sensor.closestDynamicIndex()) + sensor.obsEwVel(sensor.closestDynamicIndex()) * this.tcpa));
    }

    /** Time at closest point of approach, in seconds (LRE-Var8). */
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
