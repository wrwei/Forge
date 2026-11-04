package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;

import java.util.List;

/**
 * Functions over a multi-sensor gas reading (CD-Fn1..4). Position {@code p}
 * (1-based) of a reading is the sensor facing the {@code p}-th {@link Angle}
 * in declaration order, wrapping after four.
 */
public final class ChemicalSensor {

    private final Chem target;

    /** @param target the chemical whose source the robot searches for */
    public ChemicalSensor(Chem target) {
        this.target = target;
    }

    /**
     * Classifies a reading (CD-Fn1): {@code gasD} if some value is for the
     * target chemical with a positive intensity, {@code noGas} otherwise.
     */
    public Status analysis(List<GasSensor> readings) {
        for (GasSensor reading : readings) {
            if (reading.c().equals(target) && reading.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Peak intensity of a reading (CD-Fn2): at least every {@code i} and equal
     * to one of them. Precondition: non-empty; an empty reading yields 0.0.
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> readings) {
        if (readings.isEmpty()) {
            return 0.0;
        }
        double peak = readings.get(0).i();
        for (int index = 1; index < readings.size(); index++) {
            if (readings.get(index).i() > peak) {
                peak = readings.get(index).i();
            }
        }
        return peak;
    }

    /**
     * Direction of the first sensor whose intensity equals the peak (CD-Fn3).
     * Precondition: non-empty; an empty reading yields {@code Front}.
     */
    public Angle location(List<GasSensor> readings) {
        if (readings.isEmpty()) {
            return Angle.Front;
        }
        int strongest = 0;
        for (int index = 1; index < readings.size(); index++) {
            if (readings.get(index).i() > readings.get(strongest).i()) {
                strongest = index;
            }
        }
        return angleOf(strongest);
    }

    /** True iff {@code level} is at least {@code bound} (CD-Fn4). */
    public boolean goreq(@RoboChartType("real") double level, @RoboChartType("real") double bound) {
        return level >= bound;
    }

    private Angle angleOf(int zeroBasedPosition) {
        Angle[] directions = Angle.values();
        return directions[zeroBasedPosition % directions.length];
    }
}
