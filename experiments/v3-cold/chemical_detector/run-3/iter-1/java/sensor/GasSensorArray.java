package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;

import java.util.List;

/** Functions over gas readings used by the gas-analysis subsystem. */
public final class GasSensorArray {

    private final Chem target;

    /** Creates the function set for the given target chemical. */
    public GasSensorArray(Chem target) {
        this.target = target;
    }

    /**
     * Classifies a reading (CD-Fn1): {@code gasD} if some value is for the
     * target chemical with a positive intensity, {@code noGas} otherwise.
     */
    public Status analysis(List<GasSensor> gs) {
        for (GasSensor s : gs) {
            if (s.c().equals(target) && s.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Peak intensity of a reading (CD-Fn2). Precondition: {@code gs} is
     * non-empty; an empty reading yields 0.0.
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        if (gs.isEmpty()) {
            return 0.0;
        }
        double peak = gs.get(0).i();
        for (int x = 1; x < gs.size(); x++) {
            if (gs.get(x).i() > peak) {
                peak = gs.get(x).i();
            }
        }
        return peak;
    }

    /**
     * Direction of the strongest value of a reading (CD-Fn3): the angle of
     * the first position whose intensity equals {@link #intensity}.
     * Precondition: {@code gs} is non-empty; an empty reading yields Front.
     */
    public Angle location(List<GasSensor> gs) {
        if (gs.isEmpty()) {
            return Angle.Front;
        }
        int best = 0;
        for (int x = 1; x < gs.size(); x++) {
            if (gs.get(x).i() > gs.get(best).i()) {
                best = x;
            }
        }
        return angle(best + 1);
    }

    /** Intensity ordering (CD-Fn4): true iff {@code first >= second}. */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    /**
     * Sensing direction of the 1-based position {@code x} in a reading:
     * positions 1, 2, 3, 4 map to Left, Right, Back, Front and repeat.
     */
    public Angle angle(@RoboChartType("nat") int x) {
        Angle[] directions = Angle.values();
        if (x < 1) {
            return Angle.Front;
        }
        return directions[(x - 1) % directions.length];
    }
}
