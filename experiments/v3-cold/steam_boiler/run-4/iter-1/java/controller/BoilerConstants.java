package steam_boiler.controller;

import steam_boiler.annotation.RoboChartType;

/**
 * Constants of the boiler, the steam exit, the pumps and the cycle (SB-DM1, SB-DM2,
 * SB-DM3, SB-V3, SB-DM9). The requirements name these quantities without fixing
 * their values; the values below are invented and listed in the Layer 5 review.
 * The derived constants are written as literals and must be kept consistent with
 * the base constants they are computed from.
 */
public final class BoilerConstants {

    /** C: total capacity of the boiler (litre). */
    @RoboChartType("real") public static final double CAPACITY_C = 1000.0;
    /** M1: minimal limit quantity of water (litre). */
    @RoboChartType("real") public static final double LIMIT_M1 = 150.0;
    /** M2: maximal limit quantity of water (litre). */
    @RoboChartType("real") public static final double LIMIT_M2 = 850.0;
    /** N1: minimal normal quantity of water (litre). */
    @RoboChartType("real") public static final double NORMAL_N1 = 400.0;
    /** N2: maximal normal quantity of water (litre). */
    @RoboChartType("real") public static final double NORMAL_N2 = 600.0;

    /** W: maximal quantity of steam leaving the boiler (litre/sec). */
    @RoboChartType("real") public static final double STEAM_MAX_W = 10.0;
    /** U1: maximal gradient of increase of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_RISE_U1 = 2.0;
    /** U2: maximal gradient of decrease of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_FALL_U2 = 2.0;

    /** P: nominal capacity of one pump (litre/sec). */
    @RoboChartType("real") public static final double PUMP_CAPACITY_P = 5.0;
    /** Number of pumps (and of pump controllers). */
    @RoboChartType("nat") public static final int PUMP_COUNT = 4;

    /** Length of one cycle (sec). */
    @RoboChartType("real") public static final double CYCLE_SECONDS = 5.0;
    /** Number of successive transmissions carrying STOP that force emergency stop. */
    @RoboChartType("nat") public static final int STOP_LIMIT = 3;

    /** Derived: U1 * CYCLE_SECONDS^2 / 2, the extra steam (litre) a rising rate can release in one cycle. */
    @RoboChartType("real") public static final double STEAM_RISE_MARGIN = 25.0;
    /** Derived: U2 * CYCLE_SECONDS^2 / 2, the steam (litre) a falling rate can withhold in one cycle. */
    @RoboChartType("real") public static final double STEAM_FALL_MARGIN = 25.0;
    /** Derived: W * CYCLE_SECONDS, the most steam (litre) that can leave in one cycle. */
    @RoboChartType("real") public static final double STEAM_MAX_OUTFLOW = 50.0;
    /** Derived: PUMP_COUNT * P * CYCLE_SECONDS, the most water (litre) the pumps can supply in one cycle. */
    @RoboChartType("real") public static final double PUMPS_MAX_INFLOW = 100.0;
    /** Derived: U1 * CYCLE_SECONDS, the largest rise of the steam rate (litre/sec) between two cycles. */
    @RoboChartType("real") public static final double STEAM_RISE_PER_CYCLE = 10.0;
    /** Derived: U2 * CYCLE_SECONDS, the largest fall of the steam rate (litre/sec) between two cycles. */
    @RoboChartType("real") public static final double STEAM_FALL_PER_CYCLE = 10.0;

    private BoilerConstants() {}
}
