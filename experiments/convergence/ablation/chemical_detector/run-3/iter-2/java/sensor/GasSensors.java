package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;
import chemdetector.domain.Angle;
import chemdetector.domain.Chem;
import chemdetector.domain.GasSensor;
import chemdetector.domain.Status;
import java.util.List;

/** Gas-analysis functions over a multi-sensor reading (CD-Fn1..CD-Fn4). */
public final class GasSensors {

    /** Classifies a reading: gasD iff some sensor indicates the target chemical (CD-Fn1). */
    public Status analysis(List<GasSensor> gs) {
        for (int k = 0; k < gs.size(); k++) {
            GasSensor s = gs.get(k);
            if (s.c() == Chem.Target && s.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** Maximum intensity across the reading; 0.0 for an empty reading (CD-Fn2). */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        double max = 0.0;
        for (int k = 0; k < gs.size(); k++) {
            if (goreq(gs.get(k).i(), max)) {
                max = gs.get(k).i();
            }
        }
        return max;
    }

    /** Direction of the sensor with the highest intensity; Front for an empty reading (CD-Fn3). */
    public Angle location(List<GasSensor> gs) {
        int best = -1;
        double bestIntensity = 0.0;
        for (int k = 0; k < gs.size(); k++) {
            if (best == -1 || !goreq(bestIntensity, gs.get(k).i())) {
                best = k;
                bestIntensity = gs.get(k).i();
            }
        }
        return angle(best);
    }

    /** Intensity ordering: true iff first is at least as large as second (CD-Fn4). */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    private Angle angle(int x) {
        if (x == 0) {
            return Angle.Left;
        } else if (x == 1) {
            return Angle.Right;
        } else if (x == 2) {
            return Angle.Back;
        } else {
            return Angle.Front;
        }
    }
}
