package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;
import chemdetector.data.Angle;
import chemdetector.data.CdConstants;
import chemdetector.data.GasSensor;
import chemdetector.data.Status;
import java.util.List;

/**
 * Pure classification functions over gas-sensor readings. Each function
 * returns a safe default when the input sequence is empty, so callers
 * never need sentinel checks.
 */
public final class GasAnalyzer {

    /**
     * Classifies a reading: {@code gasD} if any sensor indicates the
     * target chemical, {@code noGas} otherwise.
     */
    public Status analysis(List<GasSensor> gs) {
        for (int idx = 0; idx < gs.size(); idx++) {
            GasSensor sample = gs.get(idx);
            if (sample.c() == CdConstants.TARGET_CHEM && sample.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Maximum intensity across the reading. Returns 0.0 for an empty
     * reading (safe default for missing data).
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        if (gs.isEmpty()) {
            return 0.0;
        }
        double peak = gs.get(0).i();
        for (int idx = 1; idx < gs.size(); idx++) {
            GasSensor sample = gs.get(idx);
            if (goreq(sample.i(), peak)) {
                peak = sample.i();
            }
        }
        return peak;
    }

    /**
     * Direction of the sensor with the highest intensity. Returns
     * {@code Front} for an empty reading (safe default for missing data).
     */
    public Angle location(List<GasSensor> gs) {
        if (gs.isEmpty()) {
            return Angle.Front;
        }
        int peakIdx = 0;
        double peak = gs.get(0).i();
        for (int idx = 1; idx < gs.size(); idx++) {
            GasSensor sample = gs.get(idx);
            if (sample.i() > peak) {
                peak = sample.i();
                peakIdx = idx;
            }
        }
        return angleOf(peakIdx);
    }

    /**
     * Intensity-greater-or-equal: true iff {@code a} is at least as
     * large as {@code b}. The only ordering predicate on intensities.
     */
    public static boolean goreq(@RoboChartType("real") double a,
                                @RoboChartType("real") double b) {
        return a >= b;
    }

    /** Maps a sensing-direction index onto a body-relative angle. */
    private static Angle angleOf(@RoboChartType("nat") int idx) {
        int quadrant = idx % 4;
        if (quadrant == 0) {
            return Angle.Front;
        }
        if (quadrant == 1) {
            return Angle.Left;
        }
        if (quadrant == 2) {
            return Angle.Back;
        }
        return Angle.Right;
    }
}
