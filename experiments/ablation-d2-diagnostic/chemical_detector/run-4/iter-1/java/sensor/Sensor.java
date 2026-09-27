package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;
import java.util.List;

/**
 * Sensor surface of the Vehicle and the gas-reading functions over it: the odometer
 * (CD-Evt3) and the analysis, intensity, location and goreq functions (CD-Fn1..4).
 * Every function returns a safe default when given an empty reading.
 */
public final class Sensor {

    private final Chem targetChem;

    @RoboChartType("real")
    private double distanceTravelled;

    public Sensor(Chem targetChem) {
        this.targetChem = targetChem;
    }

    /** Records the latest cumulative distance reported by the odometer. */
    public void updateOdometer(@RoboChartType("real") double distance) {
        this.distanceTravelled = distance;
    }

    /** Cumulative distance travelled by the Vehicle (CD-Evt3). */
    @RoboChartType("real")
    public double odometer() {
        return distanceTravelled;
    }

    /**
     * Classifies a reading (CD-Fn1): {@code gasD} if some value is for the target
     * chemical with a positive intensity, {@code noGas} otherwise.
     */
    public Status analysis(List<GasSample> readings) {
        for (var sample : readings) {
            if (sample.c().equals(targetChem) && sample.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Peak intensity of a reading (CD-Fn2). For a non-empty reading the result is at least
     * every {@code i} and equals one of them; an empty reading yields 0.
     */
    @RoboChartType("real")
    public double intensity(List<GasSample> readings) {
        if (readings.isEmpty()) {
            return 0.0;
        }
        var max = readings.get(0).i();
        for (var sample : readings) {
            if (!goreq(max, sample.i())) {
                max = sample.i();
            }
        }
        return max;
    }

    /**
     * Direction of the strongest value in a reading (CD-Fn3): {@code angle(x)} for the
     * first 1-based position {@code x} whose intensity equals {@code intensity(readings)}.
     * An empty reading yields {@code Front}.
     */
    public Angle location(List<GasSample> readings) {
        if (readings.isEmpty()) {
            return Angle.Front;
        }
        var strongest = 0;
        for (var k = 1; k < readings.size(); k++) {
            if (!goreq(readings.get(strongest).i(), readings.get(k).i())) {
                strongest = k;
            }
        }
        return angle(strongest + 1);
    }

    /**
     * Sensing direction of a 1-based sensor position: positions cycle through
     * Left, Right, Back, Front. Positions below 1 map to {@code Front}.
     */
    public Angle angle(@RoboChartType("nat") int position) {
        if (position < 1) {
            return Angle.Front;
        }
        return Angle.values()[(position - 1) % Angle.values().length];
    }

    /** Intensity ordering (CD-Fn4): true iff {@code lhs} is at least {@code rhs}. */
    public boolean goreq(@RoboChartType("real") double lhs, @RoboChartType("real") double rhs) {
        return lhs >= rhs;
    }
}
