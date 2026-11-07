package lre.operation;

import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Decides whether the AUV is inside an Object Proximity Exclusion Zone
 * (LRE-OP2): the closest static obstacle (the same selection CalcCStc makes)
 * is within MIN_SAFE_DIST, or the AUV is at or above the surface. With no
 * static obstacle the sensor reports a safe large distance.
 */
public final class CheckOPEZ {

    private final Sensor sensor;

    private boolean inOpez;

    /**
     * @param sensor the AUV sensor service
     */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes inOpez. */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= 0.0;
    }

    /** @return whether the AUV is in the exclusion zone */
    public boolean inOpez() {
        return inOpez;
    }
}
