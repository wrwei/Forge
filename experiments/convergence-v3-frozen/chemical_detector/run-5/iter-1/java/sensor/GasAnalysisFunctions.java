package chemical_detector.sensor;

import java.util.List;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;

/**
 * Pure functions over a gas reading used by the gas-analysis subsystem:
 * {@code analysis}, {@code intensity}, {@code location} and {@code goreq}.
 * Each returns a safe default for an empty reading.
 */
public final class GasAnalysisFunctions {

    /** Number of sensing directions the reading positions cycle through. */
    private static final int DIRECTIONS = 4;

    /**
     * Classifies a reading: {@code gasD} if some sensor reports the target
     * chemical with a positive intensity, {@code noGas} otherwise.
     */
    public Status analysis(List<GasSensor> readings) {
        for (var sensor : readings) {
            if (sensor.c() == Chem.TARGET && !goreq(0.0, sensor.i())) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Peak intensity of a reading: at least every {@code i} in it and equal to
     * one of them. Returns 0 for an empty reading.
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> readings) {
        if (readings.isEmpty()) {
            return 0.0;
        }
        var peak = readings.get(0).i();
        for (var sensor : readings) {
            if (!goreq(peak, sensor.i())) {
                peak = sensor.i();
            }
        }
        return peak;
    }

    /**
     * Direction of the first sensor whose intensity equals the peak intensity.
     * Returns {@code Front} for an empty reading.
     */
    public Angle location(List<GasSensor> readings) {
        var peak = intensity(readings);
        for (var index = 0; index < readings.size(); index++) {
            if (goreq(readings.get(index).i(), peak)) {
                return angle(index + 1);
            }
        }
        return Angle.Front;
    }

    /** Intensity ordering: true iff {@code lhs} is at least {@code rhs}. */
    public static boolean goreq(@RoboChartType("real") double lhs, @RoboChartType("real") double rhs) {
        return lhs >= rhs;
    }

    /**
     * Maps a 1-based sensor position to its sensing direction, clockwise from
     * the front: 1 Front, 2 Right, 3 Back, 4 Left, then repeating.
     */
    static Angle angle(@RoboChartType("nat") int position) {
        var slot = (position - 1) % DIRECTIONS;
        if (slot == 0) {
            return Angle.Front;
        } else if (slot == 1) {
            return Angle.Right;
        } else if (slot == 2) {
            return Angle.Back;
        } else {
            return Angle.Left;
        }
    }
}
