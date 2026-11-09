package syn04.constants;

import syn04.annotation.RoboChartType;

public final class SynConstants {

    @RoboChartType("real")
    public static final double moveVel = 1.0;

    @RoboChartType("real")
    public static final double threshold1 = 0.75;

    @RoboChartType("real")
    public static final double threshold2 = 1.00;

    @RoboChartType("real")
    public static final double threshold3 = 1.25;

    private SynConstants() {
    }
}
