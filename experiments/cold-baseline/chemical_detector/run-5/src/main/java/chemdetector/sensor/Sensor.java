package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.SensorService;
import chemdetector.constants.Constants;
import chemdetector.data.Angle;
import chemdetector.data.GasSensor;
import chemdetector.data.Intensity;
import chemdetector.data.Status;
import java.util.ArrayList;
import java.util.List;

/**
 * Sensor service exposing the analysis, intensity, location, and goreq
 * functions (CD-Fn1..CD-Fn4) used by the gas-analysis controller's guards
 * and entry actions, plus the most recent obstacle / odometer readings
 * for the movement controller.
 */
@SensorService
public final class Sensor {

    private List<GasSensor> latestReading = new ArrayList<>();
    @RoboChartType("real")
    private double latestOdometer = 0.0;

    /**
     * Records the most recent gas reading (set by the controller when a Gas
     * event arrives, so subsequent function calls operate on this value).
     */
    public void setReading(List<GasSensor> gs) {
        this.latestReading = gs;
    }

    /** Records the most recent odometer reading. */
    public void setOdometer(@RoboChartType("real") double d) {
        this.latestOdometer = d;
    }

    /** Returns the most recent odometer reading. */
    @RoboChartType("real")
    public double odometer() {
        return latestOdometer;
    }

    /**
     * CD-Fn1 — analysis(gs) : Status.
     * Returns gasD if any sensor reading has positive intensity, noGas otherwise.
     */
    public Status analysis(List<GasSensor> gs) {
        for (int i = 0; i < gs.size(); i++) {
            GasSensor r = gs.get(i);
            if (r.i().value() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * CD-Fn2 — intensity(gs) : Intensity.
     * Returns the maximum intensity in gs. Precondition: gs non-empty;
     * when empty, returns the zero intensity as a safe default.
     */
    public Intensity intensity(List<GasSensor> gs) {
        if (gs.isEmpty()) {
            return new Intensity(0.0);
        }
        Intensity max = gs.get(0).i();
        for (int i = 1; i < gs.size(); i++) {
            Intensity candidate = gs.get(i).i();
            if (Intensity.goreq(candidate, max)) {
                max = candidate;
            }
        }
        return max;
    }

    /**
     * CD-Fn3 — location(gs) : Angle.
     * Returns the angle of the sensor with the highest intensity.
     * Precondition: gs non-empty; when empty, returns Front as a safe default.
     */
    public Angle location(List<GasSensor> gs) {
        if (gs.isEmpty()) {
            return Angle.Front;
        }
        int bestIdx = 0;
        Intensity best = gs.get(0).i();
        for (int i = 1; i < gs.size(); i++) {
            Intensity candidate = gs.get(i).i();
            if (Intensity.goreq(candidate, best) && !Intensity.goreq(best, candidate)) {
                best = candidate;
                bestIdx = i;
            }
        }
        return angle(bestIdx);
    }

    /**
     * Maps a 0-based sensor index to a body-relative Angle.
     * Position 0 -> Front, 1 -> Left, 2 -> Right, 3 -> Back; otherwise Front.
     */
    public Angle angle(@RoboChartType("nat") int idx) {
        if (idx == 0) {
            return Angle.Front;
        }
        if (idx == 1) {
            return Angle.Left;
        }
        if (idx == 2) {
            return Angle.Right;
        }
        if (idx == 3) {
            return Angle.Back;
        }
        return Angle.Front;
    }

    /**
     * Threshold predicate used in gas-detected guards: peak intensity at or
     * above the configured threshold thr (CD-Const1, CD-Fn4).
     */
    public boolean peakAboveThreshold(List<GasSensor> gs) {
        return Intensity.goreq(intensity(gs), Constants.thr);
    }
}
