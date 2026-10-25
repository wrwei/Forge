package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.annotation.SensorService;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import java.util.List;

/**
 * Sensed values and sensor-derived functions used by the controllers:
 * the odometer reading and the classification functions over a gas reading.
 * Every function returns a safe default for an empty reading.
 */
@SensorService
public final class Sensor {

    private final Chem target;

    @RoboChartType("real")
    private double distanceTravelled;

    /** Creates a sensor surface that classifies readings against {@code target}. */
    public Sensor(Chem target) {
        this.target = target;
    }

    /** Records the latest cumulative distance reported by the Vehicle's odometer. */
    public void updateOdometer(@RoboChartType("real") double distance) {
        this.distanceTravelled = distance;
    }

    /** Cumulative distance travelled by the Vehicle. */
    @RoboChartType("real")
    public double odometer() {
        return distanceTravelled;
    }

    /** gasD if some value in {@code gs} is the target chemical with positive intensity, noGas otherwise. */
    public Status analysis(List<GasSensor> gs) {
        var result = Status.noGas;
        for (GasSensor s : gs) {
            if (s.c().equals(target) && s.i() > 0.0) {
                result = Status.gasD;
            }
        }
        return result;
    }

    /** Maximum intensity across {@code gs}; 0 for an empty reading. */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        var peak = 0.0;
        var first = true;
        for (GasSensor s : gs) {
            if (first || s.i() > peak) {
                peak = s.i();
                first = false;
            }
        }
        return peak;
    }

    /** Direction of the first sensor whose intensity equals {@code intensity(gs)}; Front for an empty reading. */
    public Angle location(List<GasSensor> gs) {
        var best = Angle.Front;
        var peak = 0.0;
        var x = 1;
        for (GasSensor s : gs) {
            if (x == 1 || s.i() > peak) {
                peak = s.i();
                best = angle(x);
            }
            x = x + 1;
        }
        return best;
    }

    /** Sensing direction of the sensor at 1-based position {@code x}, cycling through Left, Right, Back, Front. */
    public Angle angle(@RoboChartType("nat") int x) {
        var directions = Angle.values();
        return directions[Math.floorMod(x - 1, directions.length)];
    }

    /** True iff intensity {@code i1} is at least as large as intensity {@code i2}. */
    public boolean goreq(@RoboChartType("real") double i1, @RoboChartType("real") double i2) {
        return i1 >= i2;
    }
}
