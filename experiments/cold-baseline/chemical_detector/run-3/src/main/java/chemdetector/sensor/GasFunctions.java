package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.SensorService;
import chemdetector.data.Angle;
import chemdetector.data.Chem;
import chemdetector.data.GasSensor;
import chemdetector.data.Status;

import java.util.List;

/**
 * Pure gas-analysis functions (CD-Fn1..4) referenced by the
 * gas-analysis controller's transition and entry actions. The
 * functions are referenced verbatim as RoboChart function
 * declarations in the generated model.
 *
 * The "target chemical" is identified by Chem identifier 1.
 */
@SensorService
public final class GasFunctions {

    /** The chemical species the detector is searching for. */
    public static final Chem TARGET = new Chem(1);

    /**
     * CD-Fn1: classify a sequence of gas-sensor readings. Returns
     * gasD if any reading is for the target chemical; otherwise
     * noGas.
     */
    public Status analysis(List<GasSensor> gs) {
        boolean detected = false;
        for (int idx = 0; idx < gs.size(); idx = idx + 1) {
            GasSensor sample = gs.get(idx);
            if (sample.c().id() == TARGET.id()) {
                detected = true;
            }
        }
        if (detected) {
            return Status.gasD;
        } else {
            return Status.noGas;
        }
    }

    /**
     * CD-Fn2: return the maximum intensity across a non-empty
     * sequence of readings. Precondition: gs is non-empty. Safe
     * default for the empty case is Intensity(0).
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        double peak = 0.0;
        for (int idx = 0; idx < gs.size(); idx = idx + 1) {
            GasSensor sample = gs.get(idx);
            double sampleValue = sample.i();
            if (sampleValue > peak) {
                peak = sampleValue;
            }
        }
        return peak;
    }

    /**
     * CD-Fn3: return the Angle of the sensor with the highest
     * intensity. The mapping from index to Angle is:
     *   0 -> Front, 1 -> Right, 2 -> Back, 3 -> Left.
     * Safe default for the empty case is Angle.Front.
     */
    public Angle location(List<GasSensor> gs) {
        int peakIdx = 0;
        double peak = 0.0;
        for (int idx = 0; idx < gs.size(); idx = idx + 1) {
            GasSensor sample = gs.get(idx);
            double sampleValue = sample.i();
            if (sampleValue > peak) {
                peak = sampleValue;
                peakIdx = idx;
            }
        }
        return angle(peakIdx);
    }

    /**
     * CD-Fn4: goreq(a, b) returns true iff a is at least as large
     * as b. Used in the threshold check intensity(gs) >= thr.
     */
    public boolean goreq(@RoboChartType("real") double a, @RoboChartType("real") double b) {
        return a >= b;
    }

    /** Index-to-Angle mapping for the location function. */
    public Angle angle(@RoboChartType("nat") int idx) {
        if (idx == 0) {
            return Angle.Front;
        } else if (idx == 1) {
            return Angle.Right;
        } else if (idx == 2) {
            return Angle.Back;
        } else {
            return Angle.Left;
        }
    }

}
