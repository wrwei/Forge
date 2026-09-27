package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/** Selects the closest dynamic obstacle and the distance to it. */
public final class CalcCDyn {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cdyn;

    @RoboChartType("real")
    private double odistCdyn;

    public CalcCDyn(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.cdyn = sensor.closestDynamicIndex();
        this.odistCdyn = sensor.odist(this.cdyn);
    }

    @RoboChartType("nat")
    public int cdyn() {
        return cdyn;
    }

    @RoboChartType("real")
    public double odistCdyn() {
        return odistCdyn;
    }
}
