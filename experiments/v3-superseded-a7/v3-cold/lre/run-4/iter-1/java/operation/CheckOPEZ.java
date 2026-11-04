package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * The CheckOPEZ operation (LRE-OP2): the AUV is in an Object Proximity
 * Exclusion Zone when the closest static obstacle is within the minimal safe
 * distance or the AUV is at or above the surface. The closest static obstacle
 * is the one CalcCStc selects, read from the same sensor function.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    @RoboChartType("nat") private int opezTarget;
    private boolean inOpez;

    /** CheckOPEZ over the given sensor. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Computes inOpez. */
    public void compute() {
        this.opezTarget = sensor.closestStaticIndex();
        this.inOpez = sensor.odist(this.opezTarget) <= LreConstants.MIN_SAFE_DIST || sensor.depth() <= 0.0;
    }

    /** Whether the AUV is in an Object Proximity Exclusion Zone (LRE-Var1). */
    public boolean inOpez() {
        return inOpez;
    }
}
