package chemdetector.constants;

import chemdetector.annotation.RoboChartType;
import chemdetector.domain.Intensity;

/**
 * System-wide configuration constants (CD-Const1..6). Values are configured at
 * system startup and never change at run time.
 */
public final class Constants {

    /** Intensity threshold for declaring the chemical source found (CD-Const1). */
    public static final Intensity thr = new Intensity(10.0);

    /** Linear velocity for normal travel (CD-Const2). */
    @RoboChartType("real")
    public static final double lv = 1.0;

    /** Wait period inside Avoiding after a changeDirection command (CD-Const3). */
    @RoboChartType("nat")
    public static final int evadeTime = 2;

    /** Stuck-detection time window (CD-Const4). */
    @RoboChartType("nat")
    public static final int stuckPeriod = 5;

    /** Stuck-detection distance threshold (CD-Const5). */
    @RoboChartType("real")
    public static final double stuckDist = 1.0;

    /** Duration of the recovery shortRandomWalk inside GettingOut (CD-Const6). */
    @RoboChartType("nat")
    public static final int outPeriod = 3;

    private Constants() {
        // utility class — no instances
    }
}
