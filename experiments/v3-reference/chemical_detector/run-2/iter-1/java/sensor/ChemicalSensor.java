package chemical_detector.sensor;

import chemical_detector.Angle;
import chemical_detector.Chem;
import chemical_detector.GasSensor;
import chemical_detector.Status;
import chemical_detector.annotation.RoboChartType;
import java.util.List;

/**
 * The gas-analysis functions over a multi-sensor gas reading (CD-Fn1 to CD-Fn4).
 * Position k (1-based) of a reading is the sensor facing angleOf(k): Front, Right, Back,
 * Left, clockwise from the front, repeating every four sensors.
 */
public final class ChemicalSensor {

    private final Chem target;

    /**
     * @param target the chemical whose source the robot searches for
     */
    public ChemicalSensor(Chem target) {
        this.target = target;
    }

    /**
     * CD-Fn1: gasD if some sensor reports the target chemical with a positive intensity,
     * noGas otherwise (including for an empty reading).
     */
    public Status analysis(List<GasSensor> gs) {
        for (GasSensor sample : gs) {
            if (sample.c().equals(target) && sample.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * CD-Fn2: the maximum intensity across the reading. Requires a non-empty reading;
     * returns 0.0 for an empty one.
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        if (gs.isEmpty()) {
            return 0.0;
        }
        double peak = gs.get(0).i();
        for (GasSensor sample : gs) {
            if (!goreq(peak, sample.i())) {
                peak = sample.i();
            }
        }
        return peak;
    }

    /**
     * CD-Fn3: the direction of the sensor with the highest intensity; on a tie, the first
     * such sensor. Requires a non-empty reading; returns Front for an empty one.
     */
    public Angle location(List<GasSensor> gs) {
        double peak = intensity(gs);
        for (int index = 0; index < gs.size(); index++) {
            if (goreq(gs.get(index).i(), peak)) {
                return angleOf(index + 1);
            }
        }
        return Angle.Front;
    }

    /** CD-Fn4: whether the first intensity is at least as large as the second. */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    private static Angle angleOf(@RoboChartType("nat") int position) {
        int sector = (position - 1) % 4;
        if (sector == 0) {
            return Angle.Front;
        } else if (sector == 1) {
            return Angle.Right;
        } else if (sector == 2) {
            return Angle.Back;
        } else {
            return Angle.Left;
        }
    }
}
