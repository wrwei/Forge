package steam_boiler.controller;

import steam_boiler.annotation.RoboChartType;

/**
 * Constants of the boiler, the steam exit, the pumps and the control cycle.
 * The requirements name these quantities without fixing their values; the
 * values below are chosen to satisfy the stated relations (M1 below N1, N2 below
 * M2, danger five seconds beyond M1 or M2).
 */
public final class SteamBoilerConstants {

    /** C: total capacity of the boiler (litre). */
    @RoboChartType("real") public static final double CAPACITY_C = 1000.0;

    /** M1: minimal limit quantity of water (litre); W times one cycle. */
    @RoboChartType("real") public static final double LIMIT_MIN_M1 = 125.0;

    /** M2: maximal limit quantity of water (litre); C less four pumps for one cycle. */
    @RoboChartType("real") public static final double LIMIT_MAX_M2 = 840.0;

    /** N1: minimal normal quantity of water (litre). */
    @RoboChartType("real") public static final double NORMAL_MIN_N1 = 400.0;

    /** N2: maximal normal quantity of water (litre). */
    @RoboChartType("real") public static final double NORMAL_MAX_N2 = 600.0;

    /** W: maximal quantity of steam leaving the boiler (litre/sec). */
    @RoboChartType("real") public static final double STEAM_MAX_W = 25.0;

    /** U1: maximal gradient of increase of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_INCREASE_U1 = 2.0;

    /** U2: maximal gradient of decrease of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_DECREASE_U2 = 2.0;

    /** P: nominal capacity of each pump (litre/sec). */
    @RoboChartType("real") public static final double PUMP_CAPACITY_P = 8.0;

    /** Number of pumps (and of pump controllers). */
    @RoboChartType("nat") public static final int PUMP_COUNT = 4;

    /** Length of one control cycle, and the pump start-up delay (sec). */
    @RoboChartType("real") public static final double CYCLE_SECONDS = 5.0;

    /** Number of successive transmissions carrying STOP that demand emergency stop. */
    @RoboChartType("nat") public static final int STOP_TRANSMISSIONS_FOR_EMERGENCY = 3;

    private SteamBoilerConstants() {
    }
}
