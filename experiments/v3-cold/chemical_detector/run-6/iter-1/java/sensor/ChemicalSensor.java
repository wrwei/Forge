package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;

import java.util.List;

/**
 * Functions over a multi-sensor gas reading (CD-Fn1..4). A reading is a list
 * of {@link GasSensor} values; its 1-based position identifies the sensing
 * direction.
 */
public final class ChemicalSensor {
    private final Chem target;

    /** @param target the chemical whose source the robot is looking for */
    public ChemicalSensor(Chem target) {
        this.target = target;
    }

    /**
     * Classifies a reading (CD-Fn1): {@code gasD} if some value is for the
     * target chemical with a positive intensity, {@code noGas} otherwise.
     */
    public Status analysis(List<GasSensor> readings) {
        for (GasSensor reading : readings) {
            if (reading.c().equals(target) && reading.i() > 0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * The peak intensity of a reading (CD-Fn2): at least every {@code i} in the
     * reading and equal to one of them. Precondition: the reading is non-empty;
     * an empty reading yields 0.
     */
    @RoboChartType("nat")
    public int intensity(List<GasSensor> readings) {
        int peak = 0;
        for (GasSensor reading : readings) {
            if (reading.i() > peak) {
                peak = reading.i();
            }
        }
        return peak;
    }

    /**
     * The direction of the strongest value in a reading (CD-Fn3): the angle of
     * the first position whose intensity equals {@link #intensity(List)}.
     * Precondition: the reading is non-empty; an empty reading yields Front.
     */
    public Angle location(List<GasSensor> readings) {
        int peak = intensity(readings);
        for (int x = 0; x < readings.size(); x++) {
            if (readings.get(x).i() == peak) {
                return angle(x + 1);
            }
        }
        return Angle.Front;
    }

    /** Whether the first intensity is at least the second (CD-Fn4). */
    public boolean goreq(@RoboChartType("nat") int first, @RoboChartType("nat") int second) {
        return first >= second;
    }

    // Position p (1-based) faces the p-th Angle in declaration order
    // (1 Left, 2 Right, 3 Back, 4 Front), repeating for larger arrays.
    private Angle angle(@RoboChartType("nat") int position) {
        Angle[] directions = Angle.values();
        return directions[(position - 1) % directions.length];
    }
}
