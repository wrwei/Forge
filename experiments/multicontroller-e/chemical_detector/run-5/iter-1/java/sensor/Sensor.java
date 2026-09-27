package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import java.util.List;

/**
 * Sensor service: the gas-reading functions of the gas-analysis subsystem (CD-Fn1..4) and
 * the odometer sample used by the movement subsystem (CD-Evt3). Every method returns a
 * safe default when a reading is empty.
 */
public final class Sensor {

    @RoboChartType("nat")
    private final int targetChem;

    @RoboChartType("real")
    private double distance;

    public Sensor(@RoboChartType("nat") int targetChem) {
        this.targetChem = targetChem;
    }

    /** gasD iff some sensor reports the target chemical with a positive intensity (CD-Fn1). */
    public Status analysis(List<GasSensor> gs) {
        for (var reading : gs) {
            if (reading.c() == targetChem && reading.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** Peak intensity of the reading; 0 when the reading is empty (CD-Fn2). */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        var peak = 0.0;
        var first = true;
        for (var reading : gs) {
            if (first || reading.i() > peak) {
                peak = reading.i();
                first = false;
            }
        }
        return peak;
    }

    /** Direction of the first sensor holding the peak intensity; Front when empty (CD-Fn3). */
    public Angle location(List<GasSensor> gs) {
        var peak = intensity(gs);
        var position = 1;
        for (var reading : gs) {
            if (reading.i() == peak) {
                return angle(position);
            }
            position = position + 1;
        }
        return Angle.Front;
    }

    /** True iff the first intensity is at least as large as the second (CD-Fn4). */
    public boolean goreq(@RoboChartType("real") double i1, @RoboChartType("real") double i2) {
        return i1 >= i2;
    }

    /** Cumulative distance travelled, as last reported by the odometer (CD-Evt3). */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }

    /** Records a new odometer report from the Vehicle (CD-Evt3). */
    public void updateOdometer(@RoboChartType("real") double travelled) {
        this.distance = travelled;
    }

    /** Maps a 1-based sensor position to its sensing direction, cycling through Angle. */
    private static Angle angle(int position) {
        var directions = Angle.values();
        return directions[(position - 1) % directions.length];
    }
}
