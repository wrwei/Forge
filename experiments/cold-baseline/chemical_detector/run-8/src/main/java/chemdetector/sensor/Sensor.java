package chemdetector.sensor;

import java.util.ArrayList;
import java.util.List;
import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.SensorService;
import chemdetector.datatype.Angle;
import chemdetector.datatype.GasSensor;
import chemdetector.datatype.Intensity;
import chemdetector.datatype.Status;

/**
 * Sensor service exposing the analysis, intensity, location, and
 * goreq functions used by the controllers. Pure derived quantities;
 * never mutates controller state.
 */
@SensorService
public final class Sensor {

    /**
     * Classify a gas reading as noGas or gasD.
     * Returns noGas for empty input (safe default).
     */
    public Status analysis(List<GasSensor> gs) {
        if (gs == null) {
            return Status.noGas;
        }
        if (gs.isEmpty()) {
            return Status.noGas;
        }
        boolean detected = false;
        for (int idx = 0; idx < gs.size(); idx++) {
            GasSensor s = gs.get(idx);
            if (s.i().value() > 0.0) {
                detected = true;
            }
        }
        if (detected) {
            return Status.gasD;
        }
        return Status.noGas;
    }

    /**
     * Return the maximum intensity in a non-empty sequence.
     * Safe default for empty input: Intensity(0).
     */
    public Intensity intensity(List<GasSensor> gs) {
        if (gs == null) {
            return new Intensity(0.0);
        }
        if (gs.isEmpty()) {
            return new Intensity(0.0);
        }
        double best = gs.get(0).i().value();
        for (int idx = 1; idx < gs.size(); idx++) {
            double v = gs.get(idx).i().value();
            if (v > best) {
                best = v;
            }
        }
        return new Intensity(best);
    }

    /**
     * Return the Angle of the sensor with the highest intensity.
     * Safe default for empty input: Angle.Front.
     */
    public Angle location(List<GasSensor> gs) {
        if (gs == null) {
            return Angle.Front;
        }
        if (gs.isEmpty()) {
            return Angle.Front;
        }
        int bestIdx = 0;
        double best = gs.get(0).i().value();
        for (int idx = 1; idx < gs.size(); idx++) {
            double v = gs.get(idx).i().value();
            if (v > best) {
                best = v;
                bestIdx = idx;
            }
        }
        return angle(bestIdx);
    }

    /**
     * Map a 0-based sensor index to its corresponding Angle.
     */
    public Angle angle(@RoboChartType("nat") int index) {
        if (index == 0) {
            return Angle.Front;
        }
        if (index == 1) {
            return Angle.Left;
        }
        if (index == 2) {
            return Angle.Right;
        }
        return Angle.Back;
    }

    /**
     * Intensity-greater-or-equal comparison.
     */
    public boolean goreq(Intensity a, Intensity b) {
        return a.value() >= b.value();
    }

    /**
     * Make a defensive immutable copy of a gas reading.
     */
    public List<GasSensor> copyReading(List<GasSensor> gs) {
        var out = new ArrayList<GasSensor>();
        if (gs == null) {
            return out;
        }
        for (int idx = 0; idx < gs.size(); idx++) {
            out.add(gs.get(idx));
        }
        return out;
    }
}
