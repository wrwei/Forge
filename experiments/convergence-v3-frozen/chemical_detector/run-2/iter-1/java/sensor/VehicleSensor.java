package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.data.Angle;
import chemical_detector.data.Chem;
import chemical_detector.data.GasSensor;
import chemical_detector.data.Status;
import java.util.List;

/**
 * Sensing surface of the Vehicle: the odometer plus the classification,
 * intensity and steering functions over a multi-sensor gas reading.
 *
 * <p>Every query returns a safe default when no data is available, so
 * controller guards never need an existence check.
 */
public final class VehicleSensor {

    @RoboChartType("real")
    private double travelled = 0.0;

    /** Called by the platform harness when the odometer advances. */
    public void updateOdometer(@RoboChartType("real") double distance) {
        this.travelled = distance;
    }

    /** Cumulative distance travelled. Zero before the first odometer report. */
    @RoboChartType("real")
    public double odometer() {
        return travelled;
    }

    /** True iff the first intensity is at least as large as the second. */
    public boolean goreq(@RoboChartType("real") double i1, @RoboChartType("real") double i2) {
        return i1 >= i2;
    }

    /** noGas when no reading indicates the target chemical, gasD otherwise. */
    public Status analysis(List<GasSensor> gs) {
        for (var k = 0; k < gs.size(); k = k + 1) {
            if (gs.get(k).c() == Chem.TargetChem) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /** Peak intensity across the reading. Zero for an empty reading. */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        var peak = 0.0;
        for (var k = 0; k < gs.size(); k = k + 1) {
            if (goreq(gs.get(k).i(), peak)) {
                peak = gs.get(k).i();
            }
        }
        return peak;
    }

    /** Direction of the strongest reading. Front for an empty reading. */
    public Angle location(List<GasSensor> gs) {
        var best = 0;
        for (var k = 0; k < gs.size(); k = k + 1) {
            if (gs.get(k).i() > gs.get(best).i()) {
                best = k;
            }
        }
        return angle(best);
    }

    /** Sensing direction of the sensor at the given position in a reading. */
    public Angle angle(@RoboChartType("nat") int index) {
        if (index == 0) {
            return Angle.Front;
        }
        if (index == 1) {
            return Angle.Right;
        }
        if (index == 2) {
            return Angle.Back;
        }
        return Angle.Left;
    }
}
