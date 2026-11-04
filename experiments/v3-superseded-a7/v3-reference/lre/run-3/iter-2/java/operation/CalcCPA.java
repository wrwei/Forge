package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Closest Distance of Approach (cda) and Time at Closest Point of Approach
 * (tcpa) to the closest dynamic obstacle (LRE-OP5), assuming both keep their
 * current horizontal velocities and their vertical separation.
 */
public final class CalcCPA {

    private final Sensor sensor;
    @RoboChartType("nat") private int cpaIndex;
    @RoboChartType("real") private double closingNsVel;
    @RoboChartType("real") private double closingEwVel;
    @RoboChartType("real") private double tcpa;
    @RoboChartType("real") private double cda;

    /** Creates the operation over the given sensor. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * cpaIndex is the closest dynamic obstacle, the same index CalcCDyn selects
     * from the same sensor in the same step. With relative position p and
     * closing velocity c (AUV velocity minus obstacle velocity),
     * tcpa = (p . c) / |c|^2 and cda = sqrt(|p - c * tcpa|^2 + vdist^2). With no
     * dynamic obstacle, p = 0 and vdist is the sensor's safe large distance, so
     * cda is large.
     */
    public void compute() {
        this.cpaIndex = sensor.closestDynamicIndex();
        this.closingNsVel = sensor.nsVel() - sensor.obsNsVel(this.cpaIndex);
        this.closingEwVel = sensor.ewVel() - sensor.obsEwVel(this.cpaIndex);
        this.tcpa = (sensor.nsRelDist(this.cpaIndex) * this.closingNsVel
                + sensor.ewRelDist(this.cpaIndex) * this.closingEwVel)
                / (this.closingNsVel * this.closingNsVel + this.closingEwVel * this.closingEwVel);
        this.cda = Math.sqrt((sensor.nsRelDist(this.cpaIndex) - this.closingNsVel * this.tcpa)
                * (sensor.nsRelDist(this.cpaIndex) - this.closingNsVel * this.tcpa)
                + (sensor.ewRelDist(this.cpaIndex) - this.closingEwVel * this.tcpa)
                * (sensor.ewRelDist(this.cpaIndex) - this.closingEwVel * this.tcpa)
                + sensor.vdist(this.cpaIndex) * sensor.vdist(this.cpaIndex));
    }

    /** Time at closest point of approach, seconds (LRE-Var8). */
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
