package chemdetector.sensor;

import chemdetector.annotation.RoboChartType;
import chemdetector.annotation.SensorService;
import chemdetector.constants.Constants;
import chemdetector.data.Angle;
import chemdetector.data.GasSensor;
import chemdetector.data.Intensity;
import chemdetector.data.Status;
import java.util.List;

/**
 * CD-Fn1..4: pure functions over a gas-sensor reading. Each method is
 * a candidate RoboChart {@code function} declaration: the
 * gas-analysis controller invokes these methods in guard expressions
 * and entry actions, and the ETL lifts them into the per-package
 * {@code Sensors} / function set.
 */
@SensorService
public final class GasFunctions {

    /**
     * CD-Fn1: returns {@link Status#gasD} if any sensor in the reading
     * is for the target chemical, {@link Status#noGas} otherwise.
     */
    public Status analysis(List<GasSensor> seq) {
        Status result = Status.noGas;
        for (int i = 0; i < seq.size(); i++) {
            GasSensor gsItem = seq.get(i);
            if (gsItem.c().id() == Constants.TARGET_CHEM.id()) {
                result = Status.gasD;
            }
        }
        return result;
    }

    /**
     * CD-Fn2: maximum intensity in a non-empty reading. The Sensor
     * layer is responsible for never invoking this with an empty
     * sequence; if it does, the safe default of 0 is returned.
     */
    public Intensity intensity(List<GasSensor> seq) {
        double max = 0.0;
        for (int i = 0; i < seq.size(); i++) {
            GasSensor gsItem = seq.get(i);
            if (gsItem.i().value() > max) {
                max = gsItem.i().value();
            }
        }
        return new Intensity(max);
    }

    /**
     * CD-Fn3: angle associated with the highest-intensity sensor in
     * the reading. The index-to-angle mapping is the standard
     * front/right/back/left rotation around the body; reading[0] is
     * front, [1] right, [2] back, [3] left. Anything past index 3
     * wraps modulo 4. The Sensor layer is responsible for never
     * invoking this with an empty sequence; if it does, Front is
     * returned as a safe default.
     */
    public Angle location(List<GasSensor> seq) {
        int bestIdx = 0;
        double bestVal = -1.0;
        for (int i = 0; i < seq.size(); i++) {
            GasSensor gsItem = seq.get(i);
            if (gsItem.i().value() > bestVal) {
                bestVal = gsItem.i().value();
                bestIdx = i;
            }
        }
        return angle(bestIdx);
    }

    /**
     * Index-to-angle mapping referenced by CD-Fn3. Position 0 is the
     * front sensor; subsequent positions rotate clockwise around the
     * body.
     */
    public Angle angle(@RoboChartType("nat") int idx) {
        int slot = idx % 4;
        if (slot == 0) {
            return Angle.Front;
        } else if (slot == 1) {
            return Angle.Right;
        } else if (slot == 2) {
            return Angle.Back;
        } else {
            return Angle.Left;
        }
    }

    /**
     * CD-Fn4: total order on {@link Intensity}: true iff {@code a} is
     * at least as large as {@code b}.
     */
    public boolean goreq(Intensity a, Intensity b) {
        return a.value() >= b.value();
    }
}
