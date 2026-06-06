package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.SensorService;
import chemdetector.domain.Angle;
import chemdetector.domain.GasSensor;
import chemdetector.domain.Status;
import java.util.ArrayList;
import java.util.List;

/**
 * Sensor surface read by the controllers. Each method returns a safe default
 * when no data has been latched, so controller predicates can call directly
 * without sentinel checks.
 */
@SensorService
public final class Sensor {

    private List<GasSensor> latestReading = new ArrayList<GasSensor>();
    @RoboChartType("real")
    private double latestOdometer = 0.0;

    /**
     * Latches the most recent multi-sensor reading. Called by the wiring layer
     * when the Vehicle publishes a gas event.
     */
    public void setReading(List<GasSensor> gs) {
        var copy = new ArrayList<GasSensor>();
        for (int i = 0; i < gs.size(); i++) {
            copy.add(gs.get(i));
        }
        this.latestReading = copy;
    }

    /**
     * Latches the most recent odometer reading.
     */
    public void setOdometer(@RoboChartType("real") double d) {
        this.latestOdometer = d;
    }

    /**
     * Returns the latest multi-sensor reading. Safe default: empty list.
     */
    public List<GasSensor> currentReading() {
        return latestReading;
    }

    /**
     * Returns the latest cumulative odometer value. Safe default: 0.0.
     */
    @RoboChartType("real")
    public double odometer() {
        return latestOdometer;
    }

    /**
     * CD-Fn1 — analysis function. Returns gasD if any reading carries a
     * non-zero (positive) intensity, otherwise noGas. The Chem identity is
     * carried by each reading so it can be inspected by downstream logic;
     * for the formal model the predicate is "any positive intensity present".
     */
    public Status analysis(List<GasSensor> gs) {
        Status result = Status.noGas;
        for (int i = 0; i < gs.size(); i++) {
            GasSensor s = gs.get(i);
            if (s.i().value() > 0.0) {
                result = Status.gasD;
            }
        }
        return result;
    }

    /**
     * CD-Fn2 — intensity function. Precondition: gs is non-empty. Returns the
     * maximum {@code i.value} field across the sequence.
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        double peak = 0.0;
        boolean first = true;
        for (int i = 0; i < gs.size(); i++) {
            double v = gs.get(i).i().value();
            if (first) {
                peak = v;
                first = false;
            } else if (v > peak) {
                peak = v;
            }
        }
        return peak;
    }

    /**
     * CD-Fn3 — location function. Precondition: gs is non-empty. Returns the
     * angle of the sensor with the highest intensity. Index-to-angle mapping
     * is via {@link #angle(int)}.
     */
    public Angle location(List<GasSensor> gs) {
        int bestIdx = 0;
        double bestVal = 0.0;
        boolean first = true;
        for (int i = 0; i < gs.size(); i++) {
            double v = gs.get(i).i().value();
            if (first) {
                bestVal = v;
                bestIdx = i;
                first = false;
            } else if (v > bestVal) {
                bestVal = v;
                bestIdx = i;
            }
        }
        return angle(bestIdx);
    }

    /**
     * Maps a (zero-based) sensor index to the Angle relative to the robot
     * body. The mapping cycles Front, Left, Back, Right for indices 0..3.
     */
    public Angle angle(@RoboChartType("nat") int index) {
        int slot = index % 4;
        Angle a = Angle.Front;
        if (slot == 0) {
            a = Angle.Front;
        }
        if (slot == 1) {
            a = Angle.Left;
        }
        if (slot == 2) {
            a = Angle.Back;
        }
        if (slot == 3) {
            a = Angle.Right;
        }
        return a;
    }
}
