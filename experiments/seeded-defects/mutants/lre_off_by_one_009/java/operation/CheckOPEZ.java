package lre.operation;

import lre.constants.LreConstants;
import lre.sensor.Sensor;

/**
 * Determines whether the AUV is within an Object Proximity Exclusion Zone
 * (OPEZ). Sets {@code inOpez = true} when the overall distance to the
 * closest static obstacle ({@code cstc}) is at or below
 * {@link LreConstants#minSafeDist}, or the AUV depth is at or below zero.
 *
 * <p>The compute() body is a single boolean assignment — no conditional
 * branching. Reads {@code cstc} from {@link CalcCStc} so the dependency is
 * explicit. The sentinel case ({@code cstc == -1}) is handled by the Sensor
 * layer, which returns {@link Double#MAX_VALUE} from {@code odist} when no
 * static obstacle exists.
 */
public final class CheckOPEZ {

    private final Sensor sensor;
    private final CalcCStc calcCStc;

    private boolean inOpez;

    public CheckOPEZ(Sensor sensor, CalcCStc calcCStc) {
        this.sensor = sensor;
        this.calcCStc = calcCStc;
        this.inOpez = false;
    }

    public void compute() {
        this.inOpez = sensor.odist(calcCStc.cstc()) <= LreConstants.minSafeDist || sensor.depth() <= 1.0;
    }

    public boolean inOpez() {
        return inOpez;
    }
}
