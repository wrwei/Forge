package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.data.Angle;
import chemical_detector.data.Chem;
import chemical_detector.data.GasSample;
import chemical_detector.data.Status;
import java.util.List;

/**
 * Interprets multi-directional readings of the gas-sensor array. Every function
 * returns a safe default for an empty reading.
 */
public final class GasSensorArray {

    private final Chem target;

    public GasSensorArray(Chem target) {
        this.target = target;
    }

    /** The chemical the robot is searching for. */
    public Chem target() {
        return target;
    }

    /**
     * Classifies a reading: {@code gasD} if some sample reports the target
     * chemical with a positive intensity, {@code noGas} otherwise.
     */
    public Status analysis(List<GasSample> reading) {
        for (var sample : reading) {
            if (sample.c().equals(target) && sample.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** Peak intensity across the reading; {@code 0.0} for an empty reading. */
    @RoboChartType("real")
    public double intensity(List<GasSample> reading) {
        var peak = 0.0;
        var first = true;
        for (var sample : reading) {
            if (first || !goreq(peak, sample.i())) {
                peak = sample.i();
                first = false;
            }
        }
        return peak;
    }

    /**
     * Direction of the sensor reporting the peak intensity (the first one on a
     * tie); {@code Front} for an empty reading.
     */
    public Angle location(List<GasSample> reading) {
        if (reading.isEmpty()) {
            return Angle.Front;
        }
        var peak = 0.0;
        var bestPosition = 1;
        var position = 0;
        for (var sample : reading) {
            position = position + 1;
            if (position == 1 || !goreq(peak, sample.i())) {
                peak = sample.i();
                bestPosition = position;
            }
        }
        return angle(bestPosition);
    }

    /** Intensity ordering: true iff {@code lhs} is at least as large as {@code rhs}. */
    public boolean goreq(@RoboChartType("real") double lhs, @RoboChartType("real") double rhs) {
        return lhs >= rhs;
    }

    /**
     * Sensing direction of the sensor at a 1-based position in a reading.
     * Positions follow the declaration order of {@link Angle}, repeating every
     * four sensors; an invalid position maps to {@code Front}.
     */
    public Angle angle(@RoboChartType("nat") int position) {
        var index = (position - 1) % 4;
        if (index == 0) {
            return Angle.Left;
        } else if (index == 1) {
            return Angle.Right;
        } else if (index == 2) {
            return Angle.Back;
        }
        return Angle.Front;
    }
}
