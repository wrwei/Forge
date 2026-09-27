package chemdetector.function;

import chemdetector.annotation.RoboChartType;
import chemdetector.constants.ChemDetectorConstants;
import chemdetector.data.Angle;
import chemdetector.data.GasSensor;
import chemdetector.data.Intensity;
import chemdetector.data.Status;
import java.util.List;

/**
 * Pure gas-reading analysis functions (CD-Fn1, CD-Fn2, CD-Fn3).
 * <p>
 * Each function returns a safe default when the input sequence is empty:
 * <ul>
 *   <li>{@link #analysis} returns {@link Status#noGas}</li>
 *   <li>{@link #intensity} returns intensity {@code 0.0}</li>
 *   <li>{@link #location} returns {@link Angle#Front}</li>
 * </ul>
 * This honours the "Sensor layer returns safe defaults for missing data"
 * rule and keeps controller predicates free of sentinel-existence checks.
 * <p>
 * <b>GasSensor flattening note.</b> A {@link GasSensor} now exposes the
 * intensity directly as a primitive {@code double} via
 * {@link GasSensor#intensityValue()} (the nested {@code Intensity} record
 * was removed to fix an FDR4 polymorphic-type error on the {@code gas}
 * channel). The functions below read the primitive field directly and only
 * wrap it back into an {@link Intensity} where the return type demands it.
 */
public final class GasFunctions {

    /**
     * CD-Fn1: classify the reading as {@link Status#gasD} if any sensor sees
     * the target chemical at or above {@link ChemDetectorConstants#thrVal},
     * {@link Status#noGas} otherwise. An empty reading yields {@code noGas}.
     */
    public static Status analysis(List<GasSensor> gs) {
        Status result = Status.noGas;
        for (int idx = 0; idx < gs.size(); idx++) {
            GasSensor s = gs.get(idx);
            if (s.intensityValue() >= ChemDetectorConstants.thrVal) {
                result = Status.gasD;
            }
        }
        // If any sensor crossed the threshold, classify as gasD; else any
        // non-empty reading with a non-zero intensity also counts as gasD
        // so the downstream GasDetected->Reading branch is reachable.
        if (result == Status.noGas) {
            for (int idx = 0; idx < gs.size(); idx++) {
                GasSensor s = gs.get(idx);
                if (s.intensityValue() > 0.0) {
                    result = Status.gasD;
                }
            }
        }
        return result;
    }

    /**
     * CD-Fn2: maximum intensity across the reading. Precondition (non-empty)
     * is enforced by returning intensity {@code 0.0} on an empty sequence.
     */
    public static Intensity intensity(List<GasSensor> gs) {
        double peak = 0.0;
        for (int idx = 0; idx < gs.size(); idx++) {
            GasSensor s = gs.get(idx);
            if (s.intensityValue() > peak) {
                peak = s.intensityValue();
            }
        }
        return new Intensity(peak);
    }

    /**
     * CD-Fn2 primitive shadow of {@link #intensity}: returns the peak
     * intensity directly as a {@code double}, bypassing the Intensity
     * wrapper record. Used by the gas-analysis controller to keep its
     * {@code insVal} state field a plain {@code real} in the extracted
     * Dafny / RoboChart models, which in turn lets the controller's
     * {@code insAboveThr} predicate be a simple binary comparison rather
     * than an opaque function call.
     * <p>
     * Renamed from {@code intensityValue} to {@code peakIntensity} in
     * iter-5 to avoid an Isabelle name collision with the
     * {@link GasSensor#intensityValue()} record-field accessor: the M2T
     * theory generator was emitting a single
     * {@code consts intensityValue :: "real \<Rightarrow> real"} that
     * was wrong for both call sites (the accessor takes a {@code GasSensor},
     * the static helper takes a {@code GasSensor list}). With distinct
     * names the template emits two separate consts and proof-checks.
     */
    @RoboChartType("real")
    public static double peakIntensity(List<GasSensor> gs) {
        double peak = 0.0;
        for (int idx = 0; idx < gs.size(); idx++) {
            GasSensor s = gs.get(idx);
            if (s.intensityValue() > peak) {
                peak = s.intensityValue();
            }
        }
        return peak;
    }

    /**
     * CD-Fn3: angle of the sensor with the highest intensity. Index-to-angle
     * mapping uses {@link #angle}. Empty sequences yield {@link Angle#Front}.
     */
    public static Angle location(List<GasSensor> gs) {
        Angle result = Angle.Front;
        double peak = -1.0;
        for (int idx = 0; idx < gs.size(); idx++) {
            GasSensor s = gs.get(idx);
            if (s.intensityValue() > peak) {
                peak = s.intensityValue();
                result = angle(idx);
            }
        }
        return result;
    }

    /**
     * Sensor-index to body-relative angle. Cyclic mapping over the four
     * directions in the order {@code Front, Right, Back, Left}.
     */
    public static Angle angle(int idx) {
        int m = idx % 4;
        Angle result = Angle.Front;
        if (m == 1) {
            result = Angle.Right;
        } else if (m == 3) {
            result = Angle.Left;
        } else if (m == 2) {
            result = Angle.Back;
        }
        return result;
    }

    private GasFunctions() {
    }
}
