package lre.operation;

import lre.annotation.RoboChartType;
import lre.controller.LreConstants;
import lre.sensor.Sensor;

/**
 * Decides whether the AUV is in the Object Proximity Exclusion Zone (LRE-OP2):
 * within minSafeDist of the closest static obstacle, or at or above the surface.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    @RoboChartType("nat") private int opezIndex;
    private boolean inOpez;

    /** Creates the operation over the given sensor. */
    public CheckOPEZ(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     * opezIndex is the closest static obstacle, the same index CalcCStc selects
     * from the same sensor in the same step; with no static obstacle the sensor
     * reports a safe large odist.
     */
    public void compute() {
        this.opezIndex = sensor.closestStaticIndex();
        this.inOpez = sensor.odist(this.opezIndex) <= LreConstants.MIN_SAFE_DIST
                || sensor.depth() <= LreConstants.SURFACE_DEPTH;
    }

    /** Whether the AUV is in the OPEZ (LRE-Var1). */
    public boolean inOpez() {
        return inOpez;
    }
}
