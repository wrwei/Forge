package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;
import chemdetector.constants.Constants;
import chemdetector.domain.Angle;
import chemdetector.domain.GasSensor;
import chemdetector.domain.Status;

import java.util.List;

/**
 * Vehicle sensor surface. Pure functions over gas readings plus the
 * cumulative-odometer reading. The controller passes its current
 * {@code gs} state to the gas functions as a parameter; the sensor
 * holds no per-reading state of its own (it does cache the latest
 * odometer reading because the odometer is sampled by transition
 * actions rather than received as a triggering event).
 *
 * Functions exposed (CD-Fn1..4):
 *   - analysis(reading)  -> Status
 *   - intensity(reading) -> real
 *   - location(reading)  -> Angle
 *   - goreq(a, b)        -> boolean
 *
 * Direction mapping (the {@code angle(x)} function, CD-Fn3 referent):
 * the 1-based position of a sensor in the sequence maps to one of the
 * four robot-relative directions in round-robin order
 * (1 -> Front, 2 -> Right, 3 -> Back, 4 -> Left, then repeats).
 *
 * The sensor returns safe defaults for missing data: an empty gas
 * reading yields {@link Status#noGas}, peak intensity 0.0, and a
 * front-facing peak angle. Controllers may therefore call the
 * functions unconditionally without sentinel checks.
 */
public final class Sensor {

    @RoboChartType("real")
    private double latestOdometer;

    public void updateOdometer(@RoboChartType("real") double distance) {
        this.latestOdometer = distance;
    }

    @RoboChartType("real")
    public double odometer() {
        return latestOdometer;
    }

    /**
     * CD-Fn1 — analysis: noGas if no element of {@code reading}
     * indicates the target chemical, gasD otherwise.
     */
    public Status analysis(List<GasSensor> reading) {
        if (reading.isEmpty()) {
            return Status.noGas;
        }
        boolean anyDetected = false;
        for (GasSensor s : reading) {
            if (s.i() > 0.0) {
                anyDetected = true;
            }
        }
        if (anyDetected) {
            return Status.gasD;
        }
        return Status.noGas;
    }

    /**
     * CD-Fn2 — intensity: the maximum {@code i} across the reading.
     * Safe default of 0.0 when the reading is empty.
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> reading) {
        double max = 0.0;
        for (GasSensor s : reading) {
            if (s.i() > max) {
                max = s.i();
            }
        }
        return max;
    }

    /**
     * CD-Fn3 — location: the {@link Angle} of the sensor reporting
     * the maximum intensity. Returns {@link Angle#Front} when the
     * reading is empty (safe default).
     */
    public Angle location(List<GasSensor> reading) {
        if (reading.isEmpty()) {
            return Angle.Front;
        }
        int peakIdx = 0;
        double peakI = reading.get(0).i();
        for (int idx = 1; idx < reading.size(); idx++) {
            if (reading.get(idx).i() > peakI) {
                peakI = reading.get(idx).i();
                peakIdx = idx;
            }
        }
        return angle(peakIdx + 1);
    }

    /**
     * Maps a 1-based sensor index to one of the four robot-relative
     * directions in round-robin order.
     */
    public Angle angle(@RoboChartType("nat") int index) {
        int slot = ((index - 1) % 4 + 4) % 4;
        if (slot == 0) {
            return Angle.Front;
        }
        if (slot == 1) {
            return Angle.Right;
        }
        if (slot == 2) {
            return Angle.Back;
        }
        return Angle.Left;
    }

    /**
     * CD-Fn4 — goreq: true iff {@code a >= b}. Exposed as a sensor
     * function so the controller's named predicates can call it
     * directly. The threshold check goreq(ins, thr) lives in the
     * gas-analysis controller; {@link Constants#thr} is referenced
     * by the caller.
     */
    public boolean goreq(@RoboChartType("real") double a, @RoboChartType("real") double b) {
        return a >= b;
    }
}
