package lre.operation;

import lre.LreConstants;
import lre.sensor.Sensor;

/**
 * Determines whether the AUV is within an Object Proximity Exclusion Zone
 * (LRE-OP2): the closest static obstacle is within minSafeDist, or the AUV is
 * at or above the surface. The closest static obstacle is the one CalcCStc
 * selects, obtained from the same sensor function; when there is none, the
 * sensor reports a safe large distance.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private boolean inOpez;

    /** Creates the operation over the given sensor. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes inOpez. */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= 0.0;
    }

    /** True when the AUV is in the OPEZ. */
    public boolean inOpez() {
        return inOpez;
    }
}
