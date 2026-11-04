package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the time at closest point of approach (tcpa) and the closest distance of approach
 * (cda) to the closest dynamic obstacle, the one CalcCDyn stores (read from the same sensor
 * function).
 *
 * <p>With r the obstacle's horizontal position relative to the AUV (nsRelDist, ewRelDist) and
 * u the AUV's horizontal velocity relative to the obstacle (nsVel - obsNsVel, ewVel - obsEwVel):
 * tcpa = (r . u) / |u|^2 and cda = sqrt(|r - u * tcpa|^2 + vdist^2), the vertical separation
 * being taken as constant. With no dynamic obstacle the sensor gives r = 0, a zero obstacle
 * velocity and a large vdist, so cda is large.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes tcpa, then cda from it. */
    public void compute() {
        this.tcpa = (sensor.nsRelDist(sensor.closestDynamicIndex())
                        * (sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                    + sensor.ewRelDist(sensor.closestDynamicIndex())
                        * (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex())))
                / ((sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                        * (sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                    + (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex()))
                        * (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex())));
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

    /** Time at closest point of approach, seconds. */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }

    /** Closest distance of approach, metres. */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }
}
