package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach (cda) and the Time at Closest
 * Point of Approach (tcpa) to the closest dynamic obstacle, assuming both
 * keep their current horizontal velocities.
 */
public final class CalcCPA {

    private final Sensor sensor;
    // Closest dynamic obstacle, read from the same sensor function CalcCDyn uses.
    @RoboChartType("nat") private int cpaIndex;
    // Closing velocity: AUV velocity minus obstacle velocity (m/s).
    @RoboChartType("real") private double closingNs;
    @RoboChartType("real") private double closingEw;
    @RoboChartType("real") private double tcpa;
    // tcpa clamped at zero: when the closest point is already past, the closest approach is now (s).
    @RoboChartType("real") private double approachTime;
    @RoboChartType("real") private double cda;

    /** Creates the operation over the given sensor. */
    public CalcCPA(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * With p the obstacle's relative position and c the closing velocity:
     * tcpa = (p . c) / (|c|^2 + epsilon); cda is the distance at max(tcpa, 0),
     * |p - c * t| combined with the vertical separation vdist.
     */
    public void compute() {
        this.cpaIndex = sensor.closestDynamicIndex();
        this.closingNs = sensor.nsVel() - sensor.obsNsVel(this.cpaIndex);
        this.closingEw = sensor.ewVel() - sensor.obsEwVel(this.cpaIndex);
        this.tcpa = (sensor.nsRelDist(this.cpaIndex) * this.closingNs
                + sensor.ewRelDist(this.cpaIndex) * this.closingEw)
                / (this.closingNs * this.closingNs + this.closingEw * this.closingEw + LreConstants.CPA_EPSILON);
        this.approachTime = (this.tcpa + Math.sqrt(this.tcpa * this.tcpa)) / 2.0;
        this.cda = Math.sqrt(
                (sensor.nsRelDist(this.cpaIndex) - this.closingNs * this.approachTime)
                        * (sensor.nsRelDist(this.cpaIndex) - this.closingNs * this.approachTime)
                + (sensor.ewRelDist(this.cpaIndex) - this.closingEw * this.approachTime)
                        * (sensor.ewRelDist(this.cpaIndex) - this.closingEw * this.approachTime)
                + sensor.vdist(this.cpaIndex) * sensor.vdist(this.cpaIndex));
    }

    /** Closest distance of approach (m). */
    @RoboChartType("real")
    public double cda() {
        return cda;
    }

    /** Time at closest point of approach (s); negative if the closest point is past. */
    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }
}
