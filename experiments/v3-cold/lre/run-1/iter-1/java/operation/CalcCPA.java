package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the closest point of approach to the closest dynamic obstacle (LRE-OP5).
 *
 * <p>With p the obstacle's horizontal position relative to the AUV and v the obstacle's
 * velocity relative to the AUV, tcpa = -(p.v) / (|v|^2 + epsilon), and cda is the distance at
 * that time, with the vertical separation taken as constant. The obstacle is the one CalcCDyn
 * assigns to cdyn this cycle: both read the sensor's closestDynamicIndex(). With no dynamic
 * obstacle the sensor reports zero position and velocity and a large vertical distance, so
 * tcpa is 0 and cda is large.
 */
public final class CalcCPA {
    private final Sensor sensor;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa and cda for the closest dynamic obstacle. */
    public void compute() {
        this.tcpa = -(sensor.nsRelDist(sensor.closestDynamicIndex())
                        * (sensor.obsNsVel(sensor.closestDynamicIndex()) - sensor.nsVel())
                    + sensor.ewRelDist(sensor.closestDynamicIndex())
                        * (sensor.obsEwVel(sensor.closestDynamicIndex()) - sensor.ewVel()))
                / ((sensor.obsNsVel(sensor.closestDynamicIndex()) - sensor.nsVel())
                        * (sensor.obsNsVel(sensor.closestDynamicIndex()) - sensor.nsVel())
                    + (sensor.obsEwVel(sensor.closestDynamicIndex()) - sensor.ewVel())
                        * (sensor.obsEwVel(sensor.closestDynamicIndex()) - sensor.ewVel())
                    + LreConstants.CPA_SPEED_EPSILON);
        this.cda = Math.sqrt(
                (sensor.nsRelDist(sensor.closestDynamicIndex())
                        + (sensor.obsNsVel(sensor.closestDynamicIndex()) - sensor.nsVel()) * this.tcpa)
                    * (sensor.nsRelDist(sensor.closestDynamicIndex())
                        + (sensor.obsNsVel(sensor.closestDynamicIndex()) - sensor.nsVel()) * this.tcpa)
                + (sensor.ewRelDist(sensor.closestDynamicIndex())
                        + (sensor.obsEwVel(sensor.closestDynamicIndex()) - sensor.ewVel()) * this.tcpa)
                    * (sensor.ewRelDist(sensor.closestDynamicIndex())
                        + (sensor.obsEwVel(sensor.closestDynamicIndex()) - sensor.ewVel()) * this.tcpa)
                + sensor.vdist(sensor.closestDynamicIndex()) * sensor.vdist(sensor.closestDynamicIndex()));
    }

    /** Time to the closest point of approach, seconds (LRE-Var8). */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }

    /** Closest distance of approach, metres (LRE-Var7). */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }
}
