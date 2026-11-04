package lre.operation;

import lre.LreConstants;
import lre.sensor.Sensor;

/**
 * Decides whether the AUV is in an Object Proximity Exclusion Zone (LRE-OP2):
 * the closest static obstacle is within minSafeDist, or the AUV is at or above
 * the surface.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private boolean inOpez;

    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Recomputes inOpez. The closest static obstacle is the one CalcCStc
     * selects (both read sensor.closestStaticIndex() in the same step); with no
     * static obstacle, odist returns a safe large distance.
     */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= 0.0;
    }

    /** True when the AUV is in the OPEZ (LRE-Var1). */
    public boolean inOpez() { return inOpez; }
}
