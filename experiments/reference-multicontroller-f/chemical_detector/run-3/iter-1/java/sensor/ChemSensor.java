package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.annotation.SensorService;
import chemical_detector.types.Angle;
import chemical_detector.types.Chem;
import chemical_detector.types.GasSensor;
import chemical_detector.types.Status;
import java.util.List;

/**
 * Sensing surface of the Vehicle (odometer) and the pure functions over gas readings.
 * Every function returns a safe default for an empty reading.
 */
@SensorService
public final class ChemSensor {

    @RoboChartType("real")
    private double distanceTravelled;

    /** Records the latest odometer sample. */
    public void updateOdometer(@RoboChartType("real") double distance) {
        this.distanceTravelled = distance;
    }

    /** Cumulative distance travelled by the Vehicle. */
    @RoboChartType("real")
    public double odometer() {
        return distanceTravelled;
    }

    /** {@code gasD} if some value reports the target chemical with positive intensity, else {@code noGas}. */
    public Status analysis(List<GasSensor> readings) {
        for (var reading : readings) {
            if (reading.c() == Chem.TARGET && reading.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** Maximum intensity across the reading; 0 for an empty reading. */
    @RoboChartType("real")
    public double intensity(List<GasSensor> readings) {
        if (readings.isEmpty()) {
            return 0.0;
        }
        var peak = readings.get(0).i();
        for (var reading : readings) {
            if (reading.i() > peak) {
                peak = reading.i();
            }
        }
        return peak;
    }

    /** Direction of the first sensor holding the peak intensity; Front for an empty reading. */
    public Angle location(List<GasSensor> readings) {
        var peak = intensity(readings);
        for (int x = 0; x < readings.size(); x++) {
            if (readings.get(x).i() == peak) {
                return angle(x + 1);
            }
        }
        return Angle.Front;
    }

    /** True iff {@code lhs} is at least as large as {@code rhs}. */
    public boolean goreq(@RoboChartType("real") double lhs, @RoboChartType("real") double rhs) {
        return lhs >= rhs;
    }

    private static Angle angle(@RoboChartType("nat") int position) {
        var directions = Angle.values();
        return directions[(position - 1) % directions.length];
    }
}
