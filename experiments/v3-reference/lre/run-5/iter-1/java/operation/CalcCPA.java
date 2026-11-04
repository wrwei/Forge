package lre.operation;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Computes the closest distance of approach (cda) and the time at the closest
 * point of approach (tcpa) to the closest dynamic obstacle (LRE-OP5), in the
 * horizontal plane.
 *
 * <p>With p the obstacle's position relative to the AUV and w the closing
 * velocity (AUV velocity minus obstacle velocity), the separation at time t is
 * p - w*t, so tcpa = (p.w) / |w|^2 and cda = |p - w*tcpa|. CPA_EPSILON keeps
 * the denominator positive; with no relative motion tcpa is 0 and cda is the
 * current distance. The last term of cda is zero for a registered obstacle and
 * the sensor's safe large distance when there is no dynamic obstacle (whose
 * field accessors return zero).
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("real") private double cpaNsDist;
    @RoboChartType("real") private double cpaEwDist;
    @RoboChartType("real") private double cpaNsClosing;
    @RoboChartType("real") private double cpaEwClosing;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    /** Creates the operation over {@code sensor}. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa and cda from the current sensor readings. */
    public void compute() {
        this.cpaNsDist = sensor.nsRelDist(sensor.closestDynamicIndex());
        this.cpaEwDist = sensor.ewRelDist(sensor.closestDynamicIndex());
        this.cpaNsClosing = sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex());
        this.cpaEwClosing = sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex());
        this.tcpa = (this.cpaNsDist * this.cpaNsClosing + this.cpaEwDist * this.cpaEwClosing)
                / (this.cpaNsClosing * this.cpaNsClosing + this.cpaEwClosing * this.cpaEwClosing
                        + LreConstants.CPA_EPSILON);
        this.cda = Math.sqrt((this.cpaNsDist - this.cpaNsClosing * this.tcpa)
                        * (this.cpaNsDist - this.cpaNsClosing * this.tcpa)
                        + (this.cpaEwDist - this.cpaEwClosing * this.tcpa)
                        * (this.cpaEwDist - this.cpaEwClosing * this.tcpa))
                + (sensor.hdist(sensor.closestDynamicIndex())
                        - Math.sqrt(this.cpaNsDist * this.cpaNsDist + this.cpaEwDist * this.cpaEwDist));
    }

    /** Time at the closest point of approach, in seconds (LRE-Var8). */
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
