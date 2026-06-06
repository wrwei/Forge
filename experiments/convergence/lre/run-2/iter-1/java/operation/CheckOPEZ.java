package lre.operation;

import lre.constants.LreConstants;
import lre.sensor.Sensor;

/**
 * Determines whether the AUV is within an Object Proximity Exclusion
 * Zone (LRE-OP2): inOpez is true when the overall distance to the
 * closest static obstacle is at or below minSafeDist, or the AUV depth
 * is at or below zero. The no-static-obstacle case is handled by the
 * Sensor layer's safe large distance default. Invoked by the controller
 * before evaluating transition guards each step.
 */
public final class CheckOPEZ {

    private final Sensor sensor;

    private int cstcIndex = -1;

    private boolean inOpez;

    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    public void compute() {
        this.cstcIndex = sensor.closestStaticIndex();
        this.inOpez = sensor.odist(this.cstcIndex) <= LreConstants.minSafeDist
                || sensor.depth() <= 0.0;
    }

    public boolean inOpez() {
        return inOpez;
    }
}
