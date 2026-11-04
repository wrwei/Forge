package lre.operation;

import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Determines whether the AUV is in an Object Proximity Exclusion Zone (LRE-OP2):
 * the overall distance to the closest static obstacle is at or below
 * minSafeDist, or the depth is at or below zero. The closest static obstacle is
 * the index CalcCStc computes, obtained from the same sensor function.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private boolean inOpez;

    /** Creates the operation over {@code sensor}. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes inOpez from the current sensor readings. */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= 0.0;
    }

    /** Whether the AUV is in the OPEZ (LRE-Var1). */
    public boolean inOpez() {
        return inOpez;
    }
}
