package chemdetector.constants;

import chemdetector.annotation.RoboChartType;
import chemdetector.datatype.Intensity;

/**
 * Static configuration values for the Chemical Detector system.
 * <ul>
 *   <li>CD-Const1 thr: intensity threshold for declaring 'found'.</li>
 *   <li>CD-Const2 lv: linear velocity during normal travel.</li>
 *   <li>CD-Const3 evadeTime: wait inside Avoiding after changeDirection.</li>
 *   <li>CD-Const4 stuckPeriod: time window for stuck detection.</li>
 *   <li>CD-Const5 stuckDist: distance threshold for stuck detection.</li>
 *   <li>CD-Const6 outPeriod: wait inside GettingOut.</li>
 * </ul>
 */
public final class Constants {

    public static final Intensity thr = new Intensity(80.0);

    @RoboChartType("real")
    public static final double lv = 1.0;

    @RoboChartType("nat")
    public static final int evadeTime = 2;

    @RoboChartType("nat")
    public static final int stuckPeriod = 5;

    @RoboChartType("real")
    public static final double stuckDist = 1.0;

    @RoboChartType("nat")
    public static final int outPeriod = 3;

    private Constants() {
    }
}
