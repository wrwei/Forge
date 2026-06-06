package chemdetector.sensor;

import java.util.List;

import chemdetector.annotation.RoboChartType;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Chem;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;

/**
 * Gas-analysis computations over multi-sensor readings (CD-Fn1..CD-Fn4).
 * Returns safe defaults for missing data (empty readings).
 */
public final class ChemSensorService {

    private final Chem targetChem;

    public ChemSensorService(Chem targetChem) {
        this.targetChem = targetChem;
    }

    /**
     * Classifies a reading: noGas if no element indicates the target
     * chemical, gasD otherwise (CD-Fn1).
     */
    public Status analysis(List<GasSensor> gs) {
        for (GasSensor g : gs) {
            if (g.c() == targetChem && g.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Maximum intensity across the reading (CD-Fn2). Returns 0.0 for an
     * empty reading as the safe default.
     */
    public double intensity(List<GasSensor> gs) {
        double max = 0.0;
        boolean first = true;
        for (GasSensor g : gs) {
            if (first) {
                max = g.i();
                first = false;
            } else if (goreq(g.i(), max)) {
                max = g.i();
            }
        }
        return max;
    }

    /**
     * Angle of the sensor with the highest intensity (CD-Fn3). Returns
     * Front for an empty reading as the safe default.
     */
    public Angle location(List<GasSensor> gs) {
        int bestIndex = 1;
        double best = 0.0;
        boolean first = true;
        int position = 1;
        for (GasSensor g : gs) {
            if (first) {
                best = g.i();
                bestIndex = position;
                first = false;
            } else if (g.i() > best) {
                best = g.i();
                bestIndex = position;
            }
            position = position + 1;
        }
        return angle(bestIndex);
    }

    /**
     * Intensity ordering predicate: true iff the first intensity is at
     * least as large as the second (CD-Fn4).
     */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    /**
     * Maps a 1-based sensing-direction index to the corresponding Angle
     * (CD-DM7).
     */
    public Angle angle(int index) {
        if (index == 1) {
            return Angle.Front;
        }
        if (index == 2) {
            return Angle.Left;
        }
        if (index == 3) {
            return Angle.Right;
        }
        return Angle.Back;
    }
}
