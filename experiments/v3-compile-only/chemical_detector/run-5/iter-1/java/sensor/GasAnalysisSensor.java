package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.types.Angle;
import chemical_detector.types.Chem;
import chemical_detector.types.GasSample;
import chemical_detector.types.Status;
import java.util.List;

/**
 * Domain functions over a multi-sensor gas reading (CD-Fn1 to CD-Fn4).
 * Position k (1-based) of a reading comes from the sensor facing
 * direction k of Left, Right, Back, Front, wrapping round after four.
 */
public final class GasAnalysisSensor {

    private static final Angle[] DIRECTIONS = Angle.values();

    private final Chem target;

    /** Creates the functions for the given target chemical. */
    public GasAnalysisSensor(Chem target) {
        this.target = target;
    }

    /**
     * Classifies a reading (CD-Fn1): gasD if some value is for the target
     * chemical with a positive intensity, noGas otherwise (including for an
     * empty reading).
     */
    public Status analysis(List<GasSample> gs) {
        Status result = Status.noGas;
        for (GasSample sample : gs) {
            if (sample.c().equals(target) && sample.i() > 0.0) {
                result = Status.gasD;
            }
        }
        return result;
    }

    /**
     * The peak intensity of a reading (CD-Fn2): at least every value's
     * intensity, and equal to one of them. Requires a non-empty reading;
     * returns 0.0 for an empty one.
     */
    @RoboChartType("real")
    public double intensity(List<GasSample> gs) {
        if (gs.isEmpty()) {
            return 0.0;
        }
        return gs.get(peakIndex(gs)).i();
    }

    /**
     * The direction of the sensor with the peak intensity (CD-Fn3); on a tie,
     * the lowest position. Requires a non-empty reading; returns Front for an
     * empty one.
     */
    public Angle location(List<GasSample> gs) {
        if (gs.isEmpty()) {
            return Angle.Front;
        }
        return angle(peakIndex(gs) + 1);
    }

    /** True iff the first intensity is at least the second (CD-Fn4). */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    private int peakIndex(List<GasSample> gs) {
        int best = 0;
        for (int k = 1; k < gs.size(); k++) {
            if (!goreq(gs.get(best).i(), gs.get(k).i())) {
                best = k;
            }
        }
        return best;
    }

    private Angle angle(int position) {
        return DIRECTIONS[(position - 1) % DIRECTIONS.length];
    }
}
