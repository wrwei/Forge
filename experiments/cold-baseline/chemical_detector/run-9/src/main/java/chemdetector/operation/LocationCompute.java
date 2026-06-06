package chemdetector.operation;

import chemdetector.datatype.Angle;
import chemdetector.datatype.GasSensor;
import java.util.ArrayList;
import java.util.List;

/**
 * CD-Fn3 location operation. Returns the Angle corresponding to the sensor index
 * with the highest intensity. The (1-based) sensor index is mapped to Angle by
 * the angle helper: position 1 -> Front, 2 -> Right, 3 -> Back, 4 -> Left.
 * Returns Angle.Front if the input is empty.
 */
public final class LocationCompute {

    private Angle anl = Angle.Front;
    private List<GasSensor> input = new ArrayList<>();

    public LocationCompute() {
    }

    public void setInput(List<GasSensor> gs) {
        this.input = gs;
    }

    public void compute() {
        this.anl = locate(this.input);
    }

    public Angle anl() {
        return anl;
    }

    private Angle locate(List<GasSensor> gs) {
        int bestIdx = 0;
        double bestVal = -1.0;
        for (int i = 0; i < gs.size(); i++) {
            double v = gs.get(i).i().value();
            if (v > bestVal) {
                bestVal = v;
                bestIdx = i;
            }
        }
        return angleOfIndex(bestIdx);
    }

    private Angle angleOfIndex(int idx) {
        Angle result = Angle.Front;
        if (idx == 0) {
            result = Angle.Front;
        } else if (idx == 1) {
            result = Angle.Right;
        } else if (idx == 2) {
            result = Angle.Back;
        } else if (idx == 3) {
            result = Angle.Left;
        }
        return result;
    }
}
