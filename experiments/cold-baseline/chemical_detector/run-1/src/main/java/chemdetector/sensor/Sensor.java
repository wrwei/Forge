package chemdetector.sensor;

import chemdetector.annotation.SensorService;
import chemdetector.datatype.Angle;
import chemdetector.datatype.GasSensor;
import chemdetector.datatype.Intensity;
import chemdetector.datatype.Status;
import java.util.List;

/**
 * Pure-function sensor layer providing the four functions required
 * by the gas-analysis subsystem:
 *   - analysis (CD-Fn1)
 *   - intensity (CD-Fn2)
 *   - location  (CD-Fn3)
 *   - goreq     (CD-Fn4)
 *
 * The functions are referenced from guards and actions in the
 * gas-analysis controller, so the ETL lifts them as RoboChart
 * function declarations on the Sensors interface.
 */
@SensorService
public final class Sensor {

    /**
     * CD-Fn1: classify a reading.
     * Returns gasD if any reading carries the target chemical, noGas otherwise.
     * For this baseline the "target" is identified as the first reading's chem;
     * any non-zero-intensity reading with the same chem id counts as a detection.
     */
    public Status analysis(List<GasSensor> gs) {
        if (gs.isEmpty()) {
            return Status.noGas;
        }
        for (int i = 0; i < gs.size(); i++) {
            GasSensor r = gs.get(i);
            if (r.i().value() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * CD-Fn2: maximum intensity across a non-empty reading sequence.
     */
    public Intensity intensity(List<GasSensor> gs) {
        if (gs.isEmpty()) {
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

    /**
     * CD-Fn3: angle of the sensor with the highest intensity.
     * The sequence index is mapped to an Angle by {@link #angle(int)}.
     */
    public Angle location(List<GasSensor> gs) {
        if (gs.isEmpty()) {
            return Angle.Front;
        }
        int bestIdx = 0;
        double bestVal = gs.get(0).i().value();
        for (int i = 1; i < gs.size(); i++) {
            double v = gs.get(i).i().value();
            if (v > bestVal) {
                bestVal = v;
                bestIdx = i;
            }
        }
        return angle(bestIdx);
    }

    /**
     * CD-Fn4: intensity-greater-or-equal.
     * Returns true iff a is at least as large as b.
     */
    public boolean goreq(Intensity a, Intensity b) {
        return a.value() >= b.value();
    }

    /**
     * Maps a 0-based sensor index to a body-relative angle.
     * 0 -> Front, 1 -> Right, 2 -> Back, 3 -> Left (cyclic for higher indices).
     */
    public Angle angle(int idx) {
        int m = idx % 4;
        if (m == 0) {
            return Angle.Front;
        }
        if (m == 1) {
            return Angle.Right;
        }
        if (m == 2) {
            return Angle.Back;
        }
        return Angle.Left;
    }
}
