package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.SensorService;
import chemdetector.constants.ChemConstants;
import chemdetector.datamodel.Angle;
import chemdetector.datamodel.GasSensor;
import chemdetector.datamodel.Status;
import java.util.List;

/**
 * Sensor service: gas-reading classification functions and the
 * odometer reading. Returns safe defaults when no data exists.
 */
@SensorService
public final class Telemetry {

    @RoboChartType("real")
    private double distance;

    /** Test/platform hook: update the cumulative travelled distance. */
    public void updateDistance(@RoboChartType("real") double value) {
        this.distance = value;
    }

    /** Cumulative distance travelled by the Vehicle. */
    @RoboChartType("real")
    public double odometer() {
        return distance;
    }

    /**
     * Classifies a reading: gasD if any sensor indicates the target
     * chemical, noGas otherwise (including the empty reading).
     */
    public Status analysis(List<GasSensor> gs) {
        for (int k = 0; k < gs.size(); k++) {
            GasSensor g = gs.get(k);
            if (g.c() == ChemConstants.targetChem && g.i() > 0.0) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * Maximum intensity across the reading; 0.0 for the empty reading.
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        double best = 0.0;
        for (int k = 0; k < gs.size(); k++) {
            GasSensor g = gs.get(k);
            if (goreq(g.i(), best)) {
                best = g.i();
            }
        }
        return best;
    }

    /**
     * Direction of the sensor with the highest intensity; Front for the
     * empty reading.
     */
    public Angle location(List<GasSensor> gs) {
        int bestIdx = 0;
        double best = 0.0;
        for (int k = 0; k < gs.size(); k++) {
            GasSensor g = gs.get(k);
            if (goreq(g.i(), best)) {
                best = g.i();
                bestIdx = k + 1;
            }
        }
        return angle(bestIdx);
    }

    /** Maps a 1-based sensing-direction index to a body-relative angle. */
    public Angle angle(@RoboChartType("nat") int x) {
        if (x == 1) {
            return Angle.Front;
        } else if (x == 2) {
            return Angle.Right;
        } else if (x == 3) {
            return Angle.Back;
        } else if (x == 4) {
            return Angle.Left;
        } else {
            return Angle.Front;
        }
    }

    /** Intensity ordering: true iff a is at least as large as b. */
    public boolean goreq(@RoboChartType("real") double a, @RoboChartType("real") double b) {
        return a >= b;
    }
}
