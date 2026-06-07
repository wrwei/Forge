package chemdetector.sensor;

import java.util.List;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.SensorService;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.Chem;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;

/**
 * Sensing surface of the Chemical Detector: gas-reading classification
 * functions and the odometer sample. Returns safe defaults when no data
 * exists so controller predicates never need sentinel checks.
 */
@SensorService
public final class Sensor {

    /** The chemical species the robot is searching for. */
    private final Chem target = Chem.chemA;

    @RoboChartType("real")
    private double distanceTravelled = 0.0;

    /**
     * Classifies a reading: gasD if any sample reports the target
     * chemical with positive intensity, noGas otherwise.
     */
    public Status analysis(List<GasSensor> gs) {
        for (int k = 0; k < gs.size(); k++) {
            if (gs.get(k).c() == target && gs.get(k).i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Maximum intensity across the reading; 0.0 for an empty reading
     * (safe default).
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        double peak = 0.0;
        for (int k = 0; k < gs.size(); k++) {
            if (goreq(gs.get(k).i(), peak)) {
                peak = gs.get(k).i();
            }
        }
        return peak;
    }

    /**
     * Direction of the sensor with the highest intensity; Front for an
     * empty reading (safe default).
     */
    public Angle location(List<GasSensor> gs) {
        if (gs.size() == 0) {
            return Angle.Front;
        }
        int best = 0;
        for (int k = 0; k < gs.size(); k++) {
            if (gs.get(k).i() > gs.get(best).i()) {
                best = k;
            }
        }
        return angle(best);
    }

    /** Maps a 0-based sensor index to its sensing direction. */
    public Angle angle(@RoboChartType("nat") int index) {
        if (index == 0) {
            return Angle.Front;
        } else if (index == 1) {
            return Angle.Left;
        } else if (index == 2) {
            return Angle.Right;
        } else {
            return Angle.Back;
        }
    }

    /** Intensity ordering predicate: true iff first >= second. */
    public boolean goreq(@RoboChartType("real") double first,
            @RoboChartType("real") double second) {
        return first >= second;
    }

    /** Cumulative distance travelled, as reported by the odometer. */
    @RoboChartType("real")
    public double currentDistance() {
        return distanceTravelled;
    }

    /** Feeds a new odometer sample into the sensor surface. */
    public void updateDistance(@RoboChartType("real") double value) {
        this.distanceTravelled = value;
    }
}
