package lre.operation;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * CalcCPA (LRE-OP5): closest distance of approach and time to the closest point of
 * approach to the closest dynamic obstacle.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    /** CalcCPA reading the given sensor. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Recomputes tcpa and cda for the closest dynamic obstacle i (the index CalcCDyn selects,
     * through the same sensor function). With r = (nsRelDist(i), ewRelDist(i)) the obstacle's
     * position relative to the AUV and w = (nsVel - obsNsVel(i), ewVel - obsEwVel(i)) the AUV's
     * velocity relative to the obstacle, the horizontal separation at time t is r - w*t, so
     * tcpa = (r . w) / (|w|^2 + CPA_EPSILON) and cda = sqrt(|r - w*tcpa|^2 + vdist(i)^2), the
     * vertical separation held at its current value. With no dynamic obstacle, r and the obstacle
     * velocity are zero and vdist is the sensor's large distance, so tcpa = 0 and cda is large.
     */
    public void compute() {
        this.tcpa = (sensor.nsRelDist(sensor.closestDynamicIndex())
                        * (sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                    + sensor.ewRelDist(sensor.closestDynamicIndex())
                        * (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex())))
                / ((sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                        * (sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                    + (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex()))
                        * (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex()))
                    + LreConstants.CPA_EPSILON);
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

    /** Closest distance of approach, in metres (LRE-Var7). */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }

    /** Time to the closest point of approach, in seconds (LRE-Var8). */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }
}
