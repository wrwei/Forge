package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;

import java.util.List;

/**
 * Functions over a gas reading used by the gas-analysis subsystem
 * (CD-Fn1 to CD-Fn4). A reading is an ordered list of {@link GasSensor}
 * values; the 1-based position of a value encodes its sensing direction.
 */
public final class GasAnalysisSensor {

    private final Chem target;

    /**
     * @param target the chemical the robot is searching for
     */
    public GasAnalysisSensor(Chem target) {
        this.target = target;
    }

    /**
     * analysis (CD-Fn1): {@code gasD} if some value of the reading indicates
     * the target chemical (its chemical is the target and its intensity is
     * positive), {@code noGas} otherwise, in particular for an empty reading.
     */
    public Status analysis(List<GasSensor> readings) {
        Status result = Status.noGas;
        for (int k = 0; k < readings.size(); k++) {
            GasSensor value = readings.get(k);
            if (value.c().equals(target) && value.i() > 0.0) {
                result = Status.gasD;
            }
        }
        return result;
    }

    /**
     * intensity (CD-Fn2): the maximum intensity across the reading.
     * Precondition: the reading is non-empty. The result is at least every
     * value's intensity and equals one of them. For an empty reading
     * (outside the precondition) it is 0.0.
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> readings) {
        double peak = 0.0;
        for (int k = 0; k < readings.size(); k++) {
            double level = readings.get(k).i();
            if (k == 0 || !goreq(peak, level)) {
                peak = level;
            }
        }
        return peak;
    }

    /**
     * location (CD-Fn3): the direction of the sensor with the highest
     * intensity (the first such sensor if several share it).
     * Precondition: the reading is non-empty. For an empty reading
     * (outside the precondition) it is {@code Front}.
     */
    public Angle location(List<GasSensor> readings) {
        int strongest = 0;
        for (int k = 1; k < readings.size(); k++) {
            if (!goreq(readings.get(strongest).i(), readings.get(k).i())) {
                strongest = k;
            }
        }
        Angle result = Angle.Front;
        if (!readings.isEmpty()) {
            result = angle(strongest + 1);
        }
        return result;
    }

    /**
     * goreq (CD-Fn4): whether the first intensity is at least the second.
     */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    /**
     * angle(x): the sensing direction of the sensor at 1-based position x.
     * Positions follow the order of {@link Angle} (1 Left, 2 Right, 3 Back,
     * 4 Front) and repeat cyclically beyond four sensors.
     */
    private Angle angle(int position) {
        Angle[] directions = Angle.values();
        return directions[(position - 1) % directions.length];
    }
}
