package lre.operation;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Computes the closest distance of approach (cda) and the time to the closest
 * point of approach (tcpa) to the closest dynamic obstacle, in the horizontal
 * plane.
 *
 * <p>With p the obstacle's position relative to the AUV, (nsRelDist, ewRelDist),
 * and w the AUV's velocity relative to the obstacle, (nsVel - obsNsVel,
 * ewVel - obsEwVel):
 * tcpa = (p . w) / (|w|^2 + CPA_EPSILON) and
 * cda = sqrt(hdist^2 - tcpa * (p . w)), which is |p - w * tcpa|.
 * Taking |p| from hdist means that, when there is no dynamic obstacle (the
 * sensor then returns zero fields and a safe large distance), tcpa is 0 and
 * cda is the safe large distance.
 */
public final class CalcCPA {

    private final Sensor sensor;

    @RoboChartType("real")
    private double tcpa = 0.0;

    @RoboChartType("real")
    private double cda = 0.0;

    /**
     * Creates the operation over a sensor.
     *
     * @param sensor the AUV's sensor
     */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Recomputes tcpa and cda. The closest dynamic obstacle is the one CalcCDyn
     * selects: both read sensor.closestDynamicIndex() on the same readings.
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
        this.cda = Math.sqrt(sensor.hdist(sensor.closestDynamicIndex()) * sensor.hdist(sensor.closestDynamicIndex())
                - this.tcpa * (sensor.nsRelDist(sensor.closestDynamicIndex())
                        * (sensor.nsVel() - sensor.obsNsVel(sensor.closestDynamicIndex()))
                    + sensor.ewRelDist(sensor.closestDynamicIndex())
                        * (sensor.ewVel() - sensor.obsEwVel(sensor.closestDynamicIndex()))));
    }

    /**
     * Time to the closest point of approach to the closest dynamic obstacle.
     *
     * @return tcpa, seconds; negative when the closest point of approach is in the past
     */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }

    /**
     * Closest distance of approach to the closest dynamic obstacle.
     *
     * @return cda, metres
     */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }
}
