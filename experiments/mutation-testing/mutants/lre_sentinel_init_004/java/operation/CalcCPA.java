package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Computes the Closest Distance of Approach ({@code cda}) and the Time at
 * Closest Point of Approach ({@code tcpa}) to the closest dynamic obstacle
 * identified by {@code cdyn}. The compute() body contains exactly two field
 * assignments — the sentinel case (no dynamic obstacle) is handled by the
 * Sensor layer's safe defaults.
 *
 * <p>Formula (standard CPA):
 * <ul>
 *   <li>tcpa = -(rNS·vNS + rEW·vEW) / (vNS² + vEW²)</li>
 *   <li>cda  = sqrt((rNS + vNS·tcpa)² + (rEW + vEW·tcpa)²)</li>
 * </ul>
 * where rNS, rEW are relative positions and vNS, vEW are relative velocities
 * (obstacle velocity minus AUV velocity).
 */
public final class CalcCPA {

    private final Sensor sensor;
    private final CalcCDyn calcCDyn;

    @RoboChartType("real")
    private double cda;

    @RoboChartType("real")
    private double tcpa;

    public CalcCPA(Sensor sensor, CalcCDyn calcCDyn) {
        this.sensor = sensor;
        this.calcCDyn = calcCDyn;
        this.cda = Double.MAX_VALUE;
        this.tcpa = 0.0;
    }

    public void compute() {
        this.tcpa = -(sensor.nsRelDist(calcCDyn.cdyn()) * (sensor.obsNsVel(calcCDyn.cdyn()) - sensor.ns_vel())
                    + sensor.ewRelDist(calcCDyn.cdyn()) * (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ew_vel()))
                / ((sensor.obsNsVel(calcCDyn.cdyn()) - sensor.ns_vel()) * (sensor.obsNsVel(calcCDyn.cdyn()) - sensor.ns_vel())
                    + (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ew_vel()) * (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ew_vel()));
        this.cda = Math.sqrt(
                (sensor.nsRelDist(calcCDyn.cdyn()) + (sensor.obsNsVel(calcCDyn.cdyn()) - sensor.ns_vel()) * this.tcpa)
                * (sensor.nsRelDist(calcCDyn.cdyn()) + (sensor.obsNsVel(calcCDyn.cdyn()) - sensor.ns_vel()) * this.tcpa)
                + (sensor.ewRelDist(calcCDyn.cdyn()) + (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ew_vel()) * this.tcpa)
                * (sensor.ewRelDist(calcCDyn.cdyn()) + (sensor.obsEwVel(calcCDyn.cdyn()) - sensor.ew_vel()) * this.tcpa));
    }

    @RoboChartType("real")
    public double cda() {
        return cda;
    }

    @RoboChartType("real")
    public double tcpa() {
        return tcpa;
    }
}
