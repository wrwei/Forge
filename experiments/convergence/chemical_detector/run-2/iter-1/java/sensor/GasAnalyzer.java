package chemdetector.sensor;

import java.util.List;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.SensorService;
import chemdetector.constants.ChemConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;

/**
 * Gas-analysis functions over sensor readings (CD-Fn1..4). All methods
 * return safe defaults for empty readings so controller guards never need
 * sentinel checks.
 */
@SensorService
public final class GasAnalyzer {

    /** Classifies a reading: gasD iff some entry is the target chemical (CD-Fn1). */
    public Status analysis(List<GasSensor> gs) {
        for (int x = 0; x < gs.size(); x = x + 1) {
            if (gs.get(x).c() == ChemConstants.TARGET_CHEM) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** Maximum intensity across the reading; 0.0 for an empty reading (CD-Fn2). */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        double best = 0.0;
        for (int x = 0; x < gs.size(); x = x + 1) {
            if (gs.get(x).i() > best) {
                best = gs.get(x).i();
            }
        }
        return best;
    }

    /** Angle of the sensor with the highest intensity; Front for an empty reading (CD-Fn3). */
    public Angle location(List<GasSensor> gs) {
        int best = 1;
        double bestIntensity = 0.0;
        for (int x = 0; x < gs.size(); x = x + 1) {
            if (gs.get(x).i() > bestIntensity) {
                bestIntensity = gs.get(x).i();
                best = x + 1;
            }
        }
        return angle(best);
    }

    /** Maps a 1-based sensor index to its sensing direction (CD-DM7). */
    public Angle angle(@RoboChartType("nat") int x) {
        if (x == 1) {
            return Angle.Front;
        } else if (x == 2) {
            return Angle.Left;
        } else if (x == 3) {
            return Angle.Right;
        } else {
            return Angle.Back;
        }
    }

    /** Intensity greater-or-equal comparison (CD-Fn4). */
    public boolean goreq(@RoboChartType("real") double x, @RoboChartType("real") double y) {
        return x >= y;
    }
}
