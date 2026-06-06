package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.SensorService;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Chem;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;
import java.util.List;

/**
 * Pure domain functions over a gas reading (CD-Fn1..4). Referenced from
 * the gas-analysis controller's entry actions and guards, so the M2M
 * lifts them into RoboChart {@code function} declarations.
 */
@SensorService
public final class GasFunctions {

    /** analysis(gs) : Status — gasD iff a reading indicates the target (CD-Fn1). */
    public Status analysis(List<GasSensor> gs) {
        for (int i = 0; i < gs.size(); i++) {
            GasSensor s = gs.get(i);
            if (s.c() == Chem.TARGET) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** intensity(gs) : Intensity — peak intensity across the reading (CD-Fn2). */
    public double intensity(List<GasSensor> gs) {
        double max = gs.get(0).i();
        for (int i = 1; i < gs.size(); i++) {
            double v = gs.get(i).i();
            if (v > max) {
                max = v;
            }
        }
        return max;
    }

    /** location(gs) : Angle — direction of the strongest sensor (CD-Fn3). */
    public Angle location(List<GasSensor> gs) {
        int maxIdx = 0;
        double max = gs.get(0).i();
        for (int i = 1; i < gs.size(); i++) {
            double v = gs.get(i).i();
            if (v > max) {
                max = v;
                maxIdx = i;
            }
        }
        return angle(maxIdx);
    }

    /** goreq(a, b) — true iff intensity a is at least intensity b (CD-Fn4). */
    public boolean goreq(@RoboChartType("real") double a, @RoboChartType("real") double b) {
        return a >= b;
    }

    /** Maps a 1-based sensor position to a body-relative direction. */
    private Angle angle(int index) {
        if (index == 0) {
            return Angle.Front;
        }
        if (index == 1) {
            return Angle.Left;
        }
        if (index == 2) {
            return Angle.Right;
        }
        return Angle.Back;
    }
}
