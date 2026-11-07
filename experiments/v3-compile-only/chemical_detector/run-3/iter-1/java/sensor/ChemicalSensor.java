package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;

import java.util.List;

/** Domain functions over gas-sensor readings (CD-Fn1 to CD-Fn4). */
public final class ChemicalSensor {
    private final Chem target;

    /** @param target the chemical whose source the robot searches for */
    public ChemicalSensor(Chem target) {
        this.target = target;
    }

    /**
     * Classifies a reading (CD-Fn1): gasD if some sensor value is for the
     * target chemical with a positive intensity, noGas otherwise.
     */
    public Status analysis(List<GasSensor> sample) {
        for (GasSensor value : sample) {
            if (value.c().equals(target) && value.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Peak intensity of a non-empty reading (CD-Fn2): at least every
     * {@code i} in the reading and equal to one of them.
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> sample) {
        if (sample.isEmpty()) {
            throw new IllegalArgumentException("intensity requires a non-empty reading");
        }
        double peak = sample.get(0).i();
        for (GasSensor value : sample) {
            if (!goreq(peak, value.i())) {
                peak = value.i();
            }
        }
        return peak;
    }

    /**
     * Direction of the strongest sensor in a non-empty reading (CD-Fn3): the
     * angle of the first position whose intensity equals the peak.
     */
    public Angle location(List<GasSensor> sample) {
        if (sample.isEmpty()) {
            throw new IllegalArgumentException("location requires a non-empty reading");
        }
        int strongest = 1;
        for (int position = 2; position <= sample.size(); position++) {
            if (!goreq(sample.get(strongest - 1).i(), sample.get(position - 1).i())) {
                strongest = position;
            }
        }
        return directionOf(strongest);
    }

    /** Intensity ordering (CD-Fn4): true iff {@code first} is at least {@code second}. */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    /** Sensing direction of a 1-based sensor position, cycling Left, Right, Back, Front. */
    private static Angle directionOf(int position) {
        int slot = (position - 1) % 4;
        if (slot == 0) {
            return Angle.Left;
        } else if (slot == 1) {
            return Angle.Right;
        } else if (slot == 2) {
            return Angle.Back;
        } else {
            return Angle.Front;
        }
    }
}
