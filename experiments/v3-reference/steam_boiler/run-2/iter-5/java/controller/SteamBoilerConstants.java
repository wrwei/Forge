package steam_boiler.controller;

import steam_boiler.annotation.RoboChartType;

/**
 * Constants of the boiler (SB-DM1), the steam exit (SB-DM2), the pumps
 * (SB-DM3) and the cycle (SB-V3). The requirements name these quantities but
 * give no values; the values below are chosen to satisfy the stated relations
 * (M1 &lt; N1 &lt; N2 &lt; M2 &lt;= C, and N2 plus one cycle of all pumps stays below M2).
 */
public final class SteamBoilerConstants {

    /** C: total capacity of the boiler (litre). */
    @RoboChartType("real") public static final double CAPACITY_C = 1000.0;

    /** M1: minimal limit quantity of water (litre). */
    @RoboChartType("real") public static final double MINIMAL_LIMIT_M1 = 150.0;

    /** M2: maximal limit quantity of water (litre). */
    @RoboChartType("real") public static final double MAXIMAL_LIMIT_M2 = 850.0;

    /** N1: minimal normal quantity of water (litre). */
    @RoboChartType("real") public static final double MINIMAL_NORMAL_N1 = 400.0;

    /** N2: maximal normal quantity of water (litre). */
    @RoboChartType("real") public static final double MAXIMAL_NORMAL_N2 = 600.0;

    /** W: maximal quantity of steam leaving the boiler (litre/sec). */
    @RoboChartType("real") public static final double MAXIMAL_STEAM_W = 25.0;

    /** U1: maximal gradient of increase of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_INCREASE_U1 = 4.0;

    /** U2: maximal gradient of decrease of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_DECREASE_U2 = 4.0;

    /** P: nominal capacity of each pump (litre/sec). */
    @RoboChartType("real") public static final double PUMP_CAPACITY_P = 10.0;

    /** Duration of one cycle (sec). */
    @RoboChartType("real") public static final double CYCLE_SECONDS = 5.0;

    /** Number of pumps (and of pump controllers). */
    @RoboChartType("nat") public static final int PUMP_COUNT = 4;

    private SteamBoilerConstants() {}
}
