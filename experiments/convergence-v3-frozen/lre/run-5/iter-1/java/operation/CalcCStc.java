package lre.operation;

import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/** Selects the closest static obstacle and the distances to it. */
public final class CalcCStc {

    private final Sensor sensor;

    @RoboChartType("nat")
    private int cstc;

    @RoboChartType("real")
    private double hdistCstc;

    @RoboChartType("real")
    private double vdistCstc;

    @RoboChartType("real")
    private double odistCstc;

    public CalcCStc(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.cstc = sensor.closestStaticIndex();
        this.hdistCstc = sensor.hdist(this.cstc);
        this.vdistCstc = sensor.vdist(this.cstc);
        this.odistCstc = sensor.odist(this.cstc);
    }

    @RoboChartType("nat")
    public int cstc() {
        return cstc;
    }

    @RoboChartType("real")
    public double hdistCstc() {
        return hdistCstc;
    }

    @RoboChartType("real")
    public double vdistCstc() {
        return vdistCstc;
    }

    @RoboChartType("real")
    public double odistCstc() {
        return odistCstc;
    }
}
