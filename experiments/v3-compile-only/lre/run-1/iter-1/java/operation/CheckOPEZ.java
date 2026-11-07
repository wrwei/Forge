package lre.operation;

import lre.LreConstants;
import lre.annotation.RoboChartType;
import lre.sensor.Sensor;

/**
 * LRE-OP2: decides whether the AUV is in an Object Proximity Exclusion Zone:
 * the overall distance to the closest static obstacle is at or below
 * minSafeDist, or the depth is at or below zero. The closest static obstacle is
 * the one CalcCStc selects (the same sensor function, sensor.closestStaticIndex());
 * with no static obstacle the sensor returns a safe large distance.
 */
public final class CheckOPEZ {

    private final Sensor sensor;

    private boolean inOpez;

    /** The operation over the given sensor. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /** Recomputes inOpez from the sensor. */
    public void compute() {
        this.inOpez = sensor.odist(sensor.closestStaticIndex()) <= LreConstants.minSafeDist
                || sensor.depth() <= LreConstants.surfaceDepth;
    }

    /** LRE-Var1: whether the AUV is in the OPEZ. */
    public boolean inOpez() {
        return inOpez;
    }
}
