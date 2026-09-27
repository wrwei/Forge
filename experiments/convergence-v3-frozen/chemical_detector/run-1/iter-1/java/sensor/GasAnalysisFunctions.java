package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.data.Angle;
import chemical_detector.data.Chem;
import chemical_detector.data.GasSensor;
import chemical_detector.data.Status;
import java.util.List;

/**
 * The domain functions over a gas reading (CD-Fn1 .. CD-Fn4). They are pure:
 * every result is determined by the reading passed in, so the extracted
 * formal model can treat each one as an uninterpreted function.
 */
public final class GasAnalysisFunctions {

    /** Classify a reading as indicating the target chemical or not (CD-Fn1). */
    public Status analysis(List<GasSensor> reading) {
        Status result = Status.noGas;
        for (var idx = 0; idx < reading.size(); idx++) {
            if (reading.get(idx).c() == Chem.Target) {
                result = Status.gasD;
            }
        }
        return result;
    }

    /** Peak intensity across a reading (CD-Fn2). */
    @RoboChartType("real")
    public double intensity(List<GasSensor> reading) {
        double peak = 0.0;
        for (var idx = 0; idx < reading.size(); idx++) {
            if (reading.get(idx).i() > peak) {
                peak = reading.get(idx).i();
            }
        }
        return peak;
    }

    /** Direction of the strongest signal in a reading (CD-Fn3). */
    public Angle location(List<GasSensor> reading) {
        var strongest = 0;
        double peak = 0.0;
        for (var idx = 0; idx < reading.size(); idx++) {
            if (reading.get(idx).i() > peak) {
                peak = reading.get(idx).i();
                strongest = idx;
            }
        }
        return angle(strongest + 1);
    }

    /** True iff {@code i1} is at least as large as {@code i2} (CD-Fn4). */
    public boolean goreq(@RoboChartType("real") double i1, @RoboChartType("real") double i2) {
        return i1 >= i2;
    }

    /**
     * Sensing direction of the 1-based sensor position {@code position}
     * (CD-DM7: the position in the reading encodes the direction).
     */
    private Angle angle(@RoboChartType("nat") int position) {
        if (position == 1) {
            return Angle.Left;
        } else if (position == 2) {
            return Angle.Right;
        } else if (position == 3) {
            return Angle.Back;
        }
        return Angle.Front;
    }
}
