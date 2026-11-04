package lre.operation;

import lre.LreConstants;
import lre.sensor.Sensor;

/** Decides whether the AUV is in the Object Proximity Exclusion Zone (LRE-OP2). */
public final class CheckOPEZ {
    private final Sensor sensor;
    private boolean inOpez;

    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Recomputes inOpez from the closest static obstacle and the AUV depth. The index is
     * the one CalcCStc assigns to cstc this cycle: both read the sensor's closestStaticIndex().
     */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= LreConstants.SURFACE_DEPTH;
    }

    /** True when the AUV is in the exclusion zone (LRE-Var1). */
    public boolean inOpez() {
        return inOpez;
    }
}
