package lre.operation;

import lre.LreConstants;
import lre.sensor.Sensor;

/**
 * Decides whether the AUV is in an Object Proximity Exclusion Zone (LRE-OP2).
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private boolean inOpez;

    /** An operation reading from {@code sensor}. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * inOpez = odist(cstc) <= minSafeDist or depth <= 0, where cstc is the
     * closest static obstacle (the same sensor value CalcCStc stores).
     */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= 0.0;
    }

    /** Whether the AUV is in the OPEZ (LRE-Var1). */
    public boolean inOpez() {
        return inOpez;
    }
}
