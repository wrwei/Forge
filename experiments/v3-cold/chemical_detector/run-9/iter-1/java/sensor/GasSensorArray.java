package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import java.util.List;

/** Gas-sensor array of the Vehicle: classification and evaluation of a multi-sensor reading. */
public final class GasSensorArray {
    private final Chem target;

    /** Creates the array for the target chemical whose source is sought. */
    public GasSensorArray(Chem target) {
        this.target = target;
    }

    /** CD-Fn1: gasD if some value of the reading indicates the target chemical, noGas otherwise. */
    public Status analysis(List<GasSensor> gs) {
        for (int x = 0; x < gs.size(); x++) {
            GasSensor value = gs.get(x);
            if (value.c().equals(target) && value.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** CD-Fn2: the maximum intensity of a non-empty reading (0.0 for an empty one). */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        if (gs.isEmpty()) {
            return 0.0;
        }
        double peak = gs.get(0).i();
        for (int x = 1; x < gs.size(); x++) {
            if (!goreq(peak, gs.get(x).i())) {
                peak = gs.get(x).i();
            }
        }
        return peak;
    }

    /** CD-Fn3: the direction of the first sensor whose intensity is the peak (Front for an empty reading). */
    public Angle location(List<GasSensor> gs) {
        double peak = intensity(gs);
        for (int x = 0; x < gs.size(); x++) {
            if (gs.get(x).i() == peak) {
                return angle(x + 1);
            }
        }
        return Angle.Front;
    }

    /** CD-Fn4: true iff the first intensity is at least the second. */
    public boolean goreq(@RoboChartType("real") double candidate, @RoboChartType("real") double bound) {
        return candidate >= bound;
    }

    // Sensing direction of a 1-based position in the reading, cycling through Left, Right, Back, Front.
    private Angle angle(int position) {
        int slot = (position - 1) % 4;
        if (slot == 0) {
            return Angle.Left;
        } else if (slot == 1) {
            return Angle.Right;
        } else if (slot == 2) {
            return Angle.Back;
        }
        return Angle.Front;
    }
}
