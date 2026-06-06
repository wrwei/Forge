package lre.operation;

import lre.annotation.RoboChartType;
import lre.constants.LreConstants;
import lre.sensor.Sensor;

/**
 * LRE-OP2: determines whether the AUV is in an Object Proximity
 * Exclusion Zone. Sets inOpez = true when the overall distance to
 * the closest static obstacle (cstc) is at or below minSafeDist,
 * or when AUV depth is at or below zero.
 *
 * <p>This is a single boolean assignment; no conditional branching.
 * CheckOPEZ reads cstc from CalcCStc (NOT directly from
 * sensor.closestStaticIndex()) so the dependency is explicit.
 * The sentinel cstc == -1 case is handled inside the Sensor (odist
 * returns Double.MAX_VALUE), so we can call it unconditionally.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private final CalcCStc calcCStc;

    private boolean inOpez;

    public CheckOPEZ(Sensor sensor, CalcCStc calcCStc) {
        this.sensor = sensor;
        this.calcCStc = calcCStc;
    }

    public void compute() {
        this.inOpez = sensor.odist(calcCStc.cstc()) <= LreConstants.MIN_SAFE_DIST || sensor.depth() <= 0.0;
    }

    public boolean inOpez() { return inOpez; }
}
