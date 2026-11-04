package steam_boiler;

import steam_boiler.annotation.RoboChartType;

/**
 * Constant characteristics of the boiler, the steam exit and the pumps
 * (SB-DM1, SB-DM2, SB-DM3), and the fixed figures of the cycle protocol
 * (SB-V3, SB-Beh23). The requirements name the quantities but give no values
 * for C, M1, M2, N1, N2, W, U1, U2 and P; the values below are chosen to be
 * consistent with the stated ordering M1 &lt; N1 &lt; N2 &lt; M2 &lt;= C.
 */
public final class BoilerConstants {

    /** C: total capacity of the boiler (litre). */
    @RoboChartType("real")
    public static final double CAPACITY = 1000.0;

    /** M1: minimal limit quantity of water (litre). */
    @RoboChartType("real")
    public static final double MIN_LIMIT_LEVEL = 100.0;

    /** M2: maximal limit quantity of water (litre). */
    @RoboChartType("real")
    public static final double MAX_LIMIT_LEVEL = 900.0;

    /** N1: minimal normal quantity of water (litre). */
    @RoboChartType("real")
    public static final double MIN_NORMAL_LEVEL = 400.0;

    /** N2: maximal normal quantity of water (litre). */
    @RoboChartType("real")
    public static final double MAX_NORMAL_LEVEL = 600.0;

    /** W: maximal quantity of steam leaving the boiler (litre/sec). */
    @RoboChartType("real")
    public static final double MAX_STEAM_RATE = 10.0;

    /** U1: maximal gradient of increase of the steam rate (litre/sec/sec). */
    @RoboChartType("real")
    public static final double MAX_STEAM_INCREASE = 1.0;

    /** U2: maximal gradient of decrease of the steam rate (litre/sec/sec). */
    @RoboChartType("real")
    public static final double MAX_STEAM_DECREASE = 1.0;

    /** P: nominal capacity of each pump (litre/sec). */
    @RoboChartType("real")
    public static final double PUMP_CAPACITY = 10.0;

    /** Number of pumps (and of pump controllers). */
    @RoboChartType("nat")
    public static final int PUMP_COUNT = 4;

    /** Duration of one cycle, and of a pump's pressure balancing (sec). */
    @RoboChartType("real")
    public static final double CYCLE_DURATION = 5.0;

    /** Number of successive transmissions carrying STOP that force emergency stop. */
    @RoboChartType("nat")
    public static final int STOP_REPETITIONS = 3;

    private BoilerConstants() {
    }
}
