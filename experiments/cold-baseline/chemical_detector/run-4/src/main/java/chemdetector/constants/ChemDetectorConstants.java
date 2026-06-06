package chemdetector.constants;

import chemdetector.annotation.RoboChartType;
import chemdetector.datatype.Intensity;

/**
 * System-wide constants. All values are configured at startup and do not change at runtime.
 */
public final class ChemDetectorConstants {

    /** Intensity threshold for declaring a chemical source 'found' (CD-Const1). */
    public static final Intensity thr = new Intensity(10.0);

    /** Linear velocity for normal travel (CD-Const2). */
    @RoboChartType("real")
    public static final double lv = 1.0;

    /** Duration spent in Avoiding after issuing changeDirection (CD-Const3). */
    @RoboChartType("nat")
    public static final int evadeTime = 2;

    /** Stuck-detection time-window threshold (CD-Const4). */
    @RoboChartType("nat")
    public static final int stuckPeriod = 5;

    /** Stuck-detection distance threshold (CD-Const5). */
    @RoboChartType("real")
    public static final double stuckDist = 1.0;

    /** Duration of the GettingOut recovery manoeuvre (CD-Const6). */
    @RoboChartType("nat")
    public static final int outPeriod = 3;

    private ChemDetectorConstants() {}
}
