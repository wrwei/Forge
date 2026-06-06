package chemdetector.constants;

import chemdetector.annotation.RoboChartType;

/**
 * System constants per CD-Const1..6. Values are configured at
 * system startup and do not change at run time.
 */
public final class Constants {

    /** CD-Const1: intensity threshold for declaring source 'found'.
     * Modelled as a plain real for compatibility with the CSP-gen
     * constants interface (record-typed constants are not emitted
     * as numeric literals). */
    @RoboChartType("real")
    public static final double THR = 80.0;

    /** CD-Const2: linear velocity for normal travel. */
    @RoboChartType("real")
    public static final double LV = 1.0;

    /** CD-Const3: duration spent in Avoiding after changeDirection. */
    @RoboChartType("nat")
    public static final int EVADE_TIME = 5;

    /** CD-Const4: time-window threshold for stuck-detection. */
    @RoboChartType("nat")
    public static final int STUCK_PERIOD = 10;

    /** CD-Const5: distance threshold for stuck-detection. */
    @RoboChartType("real")
    public static final double STUCK_DIST = 1.0;

    /** CD-Const6: duration of the GettingOut recovery manoeuvre. */
    @RoboChartType("nat")
    public static final int OUT_PERIOD = 8;

    private Constants() {
    }
}
