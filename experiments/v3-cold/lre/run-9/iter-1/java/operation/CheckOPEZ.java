package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * Decides whether the AUV is inside an Object Proximity Exclusion Zone
 * (LRE-OP2): the closest static obstacle is within minSafeDist, or the AUV is
 * at or above the surface.
 *
 * <p>The closest static obstacle is the one CalcCStc selects:
 * {@code sensor.closestStaticIndex()}, read from the same sensor in the same
 * step. When there is no static obstacle the sensor reports a safe large
 * distance.
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

    /** Whether the AUV is in the exclusion zone (LRE-Var1). */
    public boolean inOpez() {
        return inOpez;
    }
}
