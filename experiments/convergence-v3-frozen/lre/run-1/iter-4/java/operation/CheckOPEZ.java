package lre.operation;

import lre.annotation.RoboChartType;
import lre.constants.LreConstants;
import lre.sensor.Sensor;

/** Decides whether the AUV is inside an Object Proximity Exclusion Zone. */
public final class CheckOPEZ {

    private final Sensor sensor;
    private final CalcCStc calcCStc;

    @RoboChartType("nat")
    private int cstc;

    private boolean inOpez;

    public CheckOPEZ(Sensor sensor, CalcCStc calcCStc) {
        this.sensor = sensor;
        this.calcCStc = calcCStc;
    }

    public void compute() {
        this.cstc = calcCStc.cstc();
        this.inOpez = sensor.odist(this.cstc) <= LreConstants.minSafeDist || sensor.depth() <= 0.0;
    }

    public boolean inOpez() {
        return inOpez;
    }
}
