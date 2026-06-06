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
 * Sensor-service facade exposing pure derived-quantity functions used by
 * controller guards and actions. Implements CD-Fn1..CD-Fn4 plus the
 * {@code angle(x)} helper used by CD-Fn3 (1-based index -> Angle).
 */
@SensorService
public final class Sensor {

    private List<GasSensor> currentReading;

    public Sensor() {
        this.currentReading = new ArrayList<>();
    }

    public void update(List<GasSensor> reading) {
        this.currentReading = new ArrayList<>(reading);
    }

    public List<GasSensor> currentReading() {
        return currentReading;
    }

    /**
     * CD-Fn1. analysis: classify a reading as gasD if any non-zero
     * intensity is present, noGas otherwise. The "target chemical"
     * predicate is encoded as "reading is non-empty and at least one
     * intensity is strictly positive".
     */
    public Status analysis(List<GasSensor> reading) {
        if (reading == null) {
            return Status.noGas;
        }
        for (int idx = 0; idx < reading.size(); idx++) {
            GasSensor gs = reading.get(idx);
            if (gs != null && gs.i() != null && gs.i().value() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * CD-Fn2. intensity: maximum intensity across the reading.
     * Precondition: the reading is non-empty. Safe default for an
     * empty/null reading is an Intensity of 0.0 (sensor layer absorbs
     * the missing-data case so controller predicates stay simple).
     */
    public Intensity intensity(List<GasSensor> reading) {
        if (reading == null || reading.isEmpty()) {
            return new Intensity(0.0);
        }
        double max = -1.0;
        for (int idx = 0; idx < reading.size(); idx++) {
            GasSensor gs = reading.get(idx);
            if (gs != null && gs.i() != null) {
                double v = gs.i().value();
                if (v > max) {
                    max = v;
                }
            }
        }
        if (max < 0.0) {
            return new Intensity(0.0);
        }
        return new Intensity(max);
    }

    /**
     * CD-Fn3. location: angle of the strongest sensor in the reading.
     * Returns Angle.Front as a safe default when the reading is empty.
     */
    public Angle location(List<GasSensor> reading) {
        if (reading == null || reading.isEmpty()) {
            return Angle.Front;
        }
        int bestIdx = 0;
        double bestVal = -1.0;
        for (int idx = 0; idx < reading.size(); idx++) {
            GasSensor gs = reading.get(idx);
            double v = 0.0;
            if (gs != null && gs.i() != null) {
                v = gs.i().value();
            }
            if (v > bestVal) {
                bestVal = v;
                bestIdx = idx;
            }
        }
        return angle(bestIdx + 1);
    }

    /**
     * Maps a 1-based sensor index to an Angle. Four sensors at the
     * four cardinal sides of the robot body.
     */
    public Angle angle(@RoboChartType("nat") int index) {
        int n = ((index - 1) % 4 + 4) % 4;
        if (n == 0) {
            return Angle.Front;
        }
        if (n == 1) {
            return Angle.Right;
        }
        if (n == 2) {
            return Angle.Back;
        }
        return Angle.Left;
    }
}
