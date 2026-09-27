package chemdetector.constants;

import chemdetector.annotation.RoboChartType;
import chemdetector.data.Intensity;

/**
 * System constants (CD-Const1..6). All values are fixed at startup and never
 * mutate at run time.
 */
public final class ChemDetectorConstants {

    /** CD-Const1: intensity threshold for declaring the chemical source found. */
    public static final Intensity thr = new Intensity(10.0);

    /**
     * CD-Const1 primitive shadow of {@link #thr} used by controller
     * predicates. Holding the threshold as a primitive {@code double} keeps
     * the controller's named boolean predicates as plain binary comparisons
     * on Ctrl_State fields, which (a) makes the RoboChart guard transparent
     * (no opaque {@code goreq()} function call) and (b) lets Dafny prove
     * postconditions of the form {@code insAboveThr ==> mode == Final}
     * without having to frame an uninterpreted reads-this function.
     */
    @RoboChartType("real")
    public static final double thrVal = 10.0;

    /** CD-Const2: linear velocity for normal travel. */
    @RoboChartType("real")
    public static final double lv = 2.0;

    /** CD-Const3: duration to wait inside Avoiding after changeDirection. */
    @RoboChartType("nat")
    public static final int evadeTime = 2;

    /** CD-Const4: time-window threshold for stuck detection. */
    @RoboChartType("nat")
    public static final int stuckPeriod = 5;

    /** CD-Const5: distance threshold for stuck detection. */
    @RoboChartType("real")
    public static final double stuckDist = 1.0;

    /** CD-Const6: duration of the GettingOut recovery short-random-walk. */
    @RoboChartType("nat")
    public static final int outPeriod = 3;

    private ChemDetectorConstants() {
    }
}
