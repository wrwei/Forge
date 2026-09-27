package lre.operation;

import lre.annotation.RoboChartType;
import lre.constants.LreConstants;
import lre.sensor.Sensor;

/**
 * Determines whether the AUV is inside an Object Proximity Exclusion Zone
 * (LRE-OP2). Uses the closest-static-obstacle index computed by CalcCStc.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private final CalcCStc calcCStc;

    @RoboChartType("nat")
    private int cstc = -1;

    private boolean inOpez;

    public CheckOPEZ(Sensor sensor, CalcCStc calcCStc) {
        this.sensor = sensor;
        this.calcCStc = calcCStc;
    }

    public void compute() {
        this.cstc = calcCStc.cstc();
        this.inOpez = sensor.odist(this.cstc) <= LreConstants.MIN_SAFE_DIST || sensor.depth() <= 0.0;
    }

    public boolean inOpez() {
        return inOpez;
    }
}
