package lre.operation;

import lre.LreConstants;
import lre.sensor.Sensor;

/**
 * CheckOPEZ: whether the AUV is in an Object Proximity Exclusion Zone (inOpez),
 * that is, within minSafeDist of the closest static obstacle or at or above
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
     * selects (the same sensor function, read in the same cycle); with no
     * static obstacle the sensor reports a safe large distance.
     */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= 0.0;
    }

    /** Whether the AUV is in an Object Proximity Exclusion Zone. */
    public boolean inOpez() {
        return inOpez;
    }
}
