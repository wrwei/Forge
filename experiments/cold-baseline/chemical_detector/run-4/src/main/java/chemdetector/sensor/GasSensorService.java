package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.SensorService;
import chemdetector.datatype.Angle;
import chemdetector.datatype.Chem;
import chemdetector.datatype.GasSensor;
import chemdetector.datatype.Intensity;
import chemdetector.datatype.Status;
import java.util.ArrayList;
import java.util.List;

/**
 * Sensor-layer functions used by the gas-analysis subsystem:
 * - analysis (CD-Fn1): classify a reading as noGas / gasD
 * - intensity (CD-Fn2): peak intensity over a non-empty reading
 * - location (CD-Fn3): Angle of the strongest sensor in a non-empty reading
 * - goreq (CD-Fn4): intensity-greater-or-equal comparator
 * - angle: map a 1-based sensor index to an Angle
 *
 * The 'target' chemical is fixed at construction time; the analysis function
 * checks each sensor against it for equality.
 */
@SensorService
public final class GasSensorService {

    private final Chem target;

    public GasSensorService(Chem target) {
        this.target = target;
    }

    /** CD-Fn1: classify a reading. */
    public Status analysis(List<GasSensor> gs) {
        if (gs == null) {
            return Status.noGas;
        }
        for (int i = 0; i < gs.size(); i++) {
            GasSensor s = gs.get(i);
            if (s.c().equals(target)) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** CD-Fn2: peak intensity. Precondition: gs non-empty. */
    public Intensity intensity(List<GasSensor> gs) {
        if (gs == null || gs.isEmpty()) {
            return new Intensity(0.0);
        }
        double max = gs.get(0).i().value();
        for (int i = 1; i < gs.size(); i++) {
            double v = gs.get(i).i().value();
            if (v > max) {
                max = v;
            }
        }
        return new Intensity(max);
    }

    /** CD-Fn3: Angle of the strongest sensor. Precondition: gs non-empty. */
    public Angle location(List<GasSensor> gs) {
        if (gs == null || gs.isEmpty()) {
            return Angle.Front;
        }
        int bestIdx = 0;
        double max = gs.get(0).i().value();
        for (int i = 1; i < gs.size(); i++) {
            double v = gs.get(i).i().value();
            if (v > max) {
                max = v;
                bestIdx = i;
            }
        }
        return angle(bestIdx + 1);
    }

    /** CD-Fn4: intensity greater-or-equal. */
    public boolean goreq(Intensity a, Intensity b) {
        return a.value() >= b.value();
    }

    /** Map a 1-based sensor index to a body-relative angle. */
    public Angle angle(@RoboChartType("nat") int idx) {
        Angle result;
        if (idx == 1) {
            result = Angle.Front;
        } else if (idx == 2) {
            result = Angle.Right;
        } else if (idx == 3) {
            result = Angle.Back;
        } else if (idx == 4) {
            result = Angle.Left;
        } else {
            result = Angle.Front;
        }
        return result;
    }

    /** Convenience: build an empty reading (for initial state of gs). */
    public List<GasSensor> emptyReading() {
        return new ArrayList<>();
    }
}
