package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSample;
import chemical_detector.domain.Status;

import java.util.List;

/**
 * The gas-sensor array's analysis functions (CD-Fn1..4). The position of a
 * value in a reading (1-based) is its sensing direction: positions 1, 2, 3, 4
 * face Left, Right, Back, Front, and further positions repeat that cycle.
 */
public final class ChemicalSensor {
    private final Chem target;

    /** Creates the analysis functions for the given target chemical. */
    public ChemicalSensor(Chem target) {
        this.target = target;
    }

    /**
     * analysis (CD-Fn1): {@code gasD} if some value of the reading is for the
     * target chemical with a positive intensity, {@code noGas} otherwise.
     */
    public Status analysis(List<GasSample> readings) {
        for (GasSample sample : readings) {
            if (sample.c().equals(target) && sample.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * intensity (CD-Fn2): the maximum intensity of the reading. Precondition:
     * the reading is non-empty; an empty reading yields 0.0.
     */
    @RoboChartType("real")
    public double intensity(List<GasSample> readings) {
        if (readings.isEmpty()) {
            return 0.0;
        }
        double peak = readings.get(0).i();
        for (GasSample sample : readings) {
            if (sample.i() > peak) {
                peak = sample.i();
            }
        }
        return peak;
    }

    /**
     * location (CD-Fn3): the direction of the first sensor whose intensity
     * equals {@link #intensity(List)}. Precondition: the reading is non-empty;
     * an empty reading yields Front.
     */
    public Angle location(List<GasSample> readings) {
        if (readings.isEmpty()) {
            return Angle.Front;
        }
        int strongest = 0;
        for (int x = 1; x < readings.size(); x++) {
            if (readings.get(x).i() > readings.get(strongest).i()) {
                strongest = x;
            }
        }
        return angle(strongest);
    }

    /** goreq (CD-Fn4): true iff the first intensity is at least the second. */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    private static Angle angle(int index) {
        Angle[] directions = Angle.values();
        return directions[index % directions.length];
    }
}
