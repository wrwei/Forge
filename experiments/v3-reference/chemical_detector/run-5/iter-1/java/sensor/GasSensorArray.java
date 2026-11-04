package chemical_detector.sensor;

import chemical_detector.annotation.RoboChartType;
import chemical_detector.domain.Angle;
import chemical_detector.domain.Chem;
import chemical_detector.domain.GasSensor;
import chemical_detector.domain.Status;
import java.util.List;

/**
 * The gas-sensor array: the functions that classify and summarise a multi-directional gas reading.
 */
public final class GasSensorArray {

    private final Chem targetChemical;

    /**
     * Creates the sensor array for one target chemical.
     *
     * @param targetChemical the chemical whose source the robot searches for
     */
    public GasSensorArray(Chem targetChemical) {
        this.targetChemical = targetChemical;
    }

    /**
     * analysis: classifies a reading.
     *
     * @param gs the reading
     * @return gasD if some value reports the target chemical with a positive intensity, noGas otherwise
     */
    public Status analysis(List<GasSensor> gs) {
        for (GasSensor value : gs) {
            if (value.c().equals(targetChemical) && !goreq(0.0, value.i())) {
                return Status.gasD;
            }
        }
        return Status.noGas;
    }

    /**
     * intensity: the peak intensity of a reading. Precondition: gs is non-empty.
     *
     * @param gs the reading
     * @return the largest i in gs (0.0 if gs is empty)
     */
    @RoboChartType("real")
    public double intensity(List<GasSensor> gs) {
        if (gs.isEmpty()) {
            return 0.0;
        }
        double peak = gs.get(0).i();
        for (GasSensor value : gs) {
            if (!goreq(peak, value.i())) {
                peak = value.i();
            }
        }
        return peak;
    }

    /**
     * location: the direction of the strongest signal. Precondition: gs is non-empty.
     *
     * @param gs the reading
     * @return angle(x) for the first 1-based position x whose intensity equals intensity(gs)
     *         (Front if gs is empty)
     */
    public Angle location(List<GasSensor> gs) {
        if (gs.isEmpty()) {
            return Angle.Front;
        }
        int strongest = 0;
        for (int x = 1; x < gs.size(); x++) {
            if (!goreq(gs.get(strongest).i(), gs.get(x).i())) {
                strongest = x;
            }
        }
        return angle(strongest + 1);
    }

    /**
     * goreq: intensity greater-or-equal.
     *
     * @param first the first intensity
     * @param second the second intensity
     * @return true iff first is at least as large as second
     */
    public boolean goreq(@RoboChartType("real") double first, @RoboChartType("real") double second) {
        return first >= second;
    }

    /**
     * angle: the sensing direction of a 1-based sensor position, clockwise from the front
     * (1 Front, 2 Right, 3 Back, 4 Left, then repeating).
     *
     * @param x the 1-based sensor position
     * @return the direction that position faces (Front for a position below 1)
     */
    public Angle angle(@RoboChartType("nat") int x) {
        if (x < 1) {
            return Angle.Front;
        }
        int slot = (x - 1) % 4;
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
}
