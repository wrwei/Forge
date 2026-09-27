package lre.operation;

import lre.annotation.RoboChartType;
import lre.constants.LreConstants;
import lre.sensor.Sensor;

/**
 * Decides whether the AUV is inside an Object Proximity Exclusion Zone
 * (LRE-OP2, LRE-Var1). The zone is entered when the closest static obstacle
 * is at or within the minimal safe distance, or when the AUV has reached the
 * surface.
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

    /** Recomputes the exclusion-zone status. */
    public void compute() {
        this.cstc = calcCStc.cstc();
        this.inOpez = sensor.odist(this.cstc) <= LreConstants.minSafeDist || sensor.depth() <= 0.0;
    }

    /** True when the AUV is inside the Object Proximity Exclusion Zone. */
    public boolean inOpez() {
        return inOpez;
    }
}
