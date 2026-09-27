package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.annotation.SensorService;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import java.util.List;

/**
 * Sensor surface of the Vehicle: the odometer reading and the functions that
 * classify a multi-sensor gas reading. Every function returns a safe default for
 * an empty reading.
 */
@SensorService
public final class Sensor {

    private final Chem targetChem;

    @RoboChartType("real")
    private double distanceTravelled;

    public Sensor(Chem targetChem) {
        this.targetChem = targetChem;
    }

    /** Records the latest cumulative distance reported by the odometer. */
    public void updateOdometer(@RoboChartType("real") double distance) {
        this.distanceTravelled = distance;
    }

    /** Cumulative distance travelled by the Vehicle. */
    @RoboChartType("real")
    public double odometer() {
        return distanceTravelled;
    }

    /**
     * Classifies a reading: {@code gasD} if some entry reports the target chemical
     * with positive intensity, {@code noGas} otherwise (including an empty reading).
     */
    public Status analysis(List<GasSensor> gs) {
        for (GasSensor s : gs) {
            if (s.c().equals(targetChem) && s.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** Peak intensity across the reading; 0 for an empty reading. */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        if (gs.isEmpty()) {
            return 0.0;
        }
        double peak = gs.get(0).i();
        for (int x = 1; x < gs.size(); x++) {
            if (goreq(gs.get(x).i(), peak)) {
                peak = gs.get(x).i();
            }
        }
        return peak;
    }

    /**
     * Direction of the first sensor carrying the peak intensity; {@code Front} for an
     * empty reading.
     */
    public Angle location(List<GasSensor> gs) {
        if (gs.isEmpty()) {
            return Angle.Front;
        }
        int best = 0;
        for (int x = 1; x < gs.size(); x++) {
            if (!goreq(gs.get(best).i(), gs.get(x).i())) {
                best = x;
            }
        }
        return angle(best);
    }

    /** True iff intensity {@code i1} is at least as large as {@code i2}. */
    public boolean goreq(@RoboChartType("real") double i1, @RoboChartType("real") double i2) {
        return i1 >= i2;
    }

    /**
     * Sensing direction of the sensor at 0-based position {@code x}: positions follow
     * the declaration order of {@link Angle} and wrap around for larger arrays.
     */
    private Angle angle(int x) {
        Angle[] directions = Angle.values();
        return directions[x % directions.length];
    }
}
