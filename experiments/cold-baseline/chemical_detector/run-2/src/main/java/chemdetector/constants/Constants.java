package chemdetector.constants;

import chemdetector.annotation.RoboChartType;
import chemdetector.data.Chem;
import chemdetector.data.Intensity;

/**
 * Domain constants for the Chemical Detector. CD-Const1..6.
 */
public final class Constants {

    private Constants() {
    }

    /** CD-DM4 anchor: the chemical the robot is searching for. */
    public static final Chem TARGET_CHEM = new Chem(1);

    /** CD-Const1: intensity threshold for declaring the source found. */
    public static final Intensity thr = new Intensity(80.0);

    /** CD-Const2: linear velocity used by every move() call. */
    @RoboChartType("real")
    public static final double lv = 1.0;

    /** CD-Const3: time spent in Avoiding after a changeDirection. */
    @RoboChartType("nat")
    public static final int evadeTime = 5;

    /** CD-Const4: time window for stuck detection. */
    @RoboChartType("nat")
    public static final int stuckPeriod = 10;

    /** CD-Const5: distance threshold for stuck detection. */
    @RoboChartType("real")
    public static final double stuckDist = 2.0;

    /** CD-Const6: bounded duration spent inside GettingOut. */
    @RoboChartType("nat")
    public static final int outPeriod = 5;
}
