package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasMeasurement;
import chemical_detector.domain.Status;
import java.util.List;

/**
 * The gas-sensor array: classifies multi-directional readings for one target chemical.
 * Position k (1-based) of a reading is the sensor facing direction angle(k).
 */
public final class GasSensorArray {

    private final Chem target;

    /** @param target the chemical whose source the robot searches for */
    public GasSensorArray(Chem target) {
        this.target = target;
    }

    /** gasD if some sample reports the target chemical with positive intensity, noGas otherwise. */
    public Status analysis(List<GasMeasurement> gs) {
        for (int k = 0; k < gs.size(); k++) {
            GasMeasurement sample = gs.get(k);
            if (sample.c().id() == target.id() && sample.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** The maximum intensity across a non-empty reading (0.0 for an empty one). */
    @RoboChartType("real")
    public double intensity(List<GasMeasurement> gs) {
        if (gs.isEmpty()) {
            return 0.0;
        }
        double peak = gs.get(0).i();
        for (int k = 1; k < gs.size(); k++) {
            if (gs.get(k).i() > peak) {
                peak = gs.get(k).i();
            }
        }
        return peak;
    }

    /** The direction of the first sample with the maximum intensity (Front for an empty reading). */
    public Angle location(List<GasMeasurement> gs) {
        if (gs.isEmpty()) {
            return Angle.Front;
        }
        int strongest = 0;
        for (int k = 1; k < gs.size(); k++) {
            if (gs.get(k).i() > gs.get(strongest).i()) {
                strongest = k;
            }
        }
        return angleAt(strongest + 1);
    }

    /** True iff the first intensity is at least as large as the second. */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    // angle(x): positions 1, 2, 3, 4 face Left, Right, Back, Front, repeating for larger arrays.
    private Angle angleAt(int position) {
        int sector = (position - 1) % 4;
        if (sector == 0) {
            return Angle.Left;
        } else if (sector == 1) {
            return Angle.Right;
        } else if (sector == 2) {
            return Angle.Back;
        } else {
            return Angle.Front;
        }
    }
}
