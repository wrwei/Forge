package lre.operation;

import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Decides whether the AUV is inside an Object Proximity Exclusion Zone: the
 * overall distance to the closest static obstacle is at or below the minimal
 * safe distance, or the AUV is at or above the surface.
 */
public final class CheckOPEZ {

    private final Sensor sensor;

    private boolean inOpez = false;

    /**
     * Creates the operation over a sensor.
     *
     * @param sensor the AUV's sensor
     */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * Recomputes inOpez. The closest static obstacle is the one CalcCStc
     * selects: both read sensor.closestStaticIndex() on the same readings.
     */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= LreConstants.SURFACE_DEPTH;
    }

    /**
     * Whether the AUV is in the Object Proximity Exclusion Zone.
     *
     * @return inOpez
     */
    public boolean inOpez() {
        return inOpez;
    }
}
