package steam_boiler.controller;

import steam_boiler.annotation.RoboChartType;

/**
 * Physical constants of the boiler, the steam exit and the pumps. The requirements
 * name these quantities but give no values; the values below are chosen so that
 * M1 &lt; N1 &lt; N2 &lt; M2 &lt; C and so that one cycle of worst-case evolution from
 * the normal band cannot reach a limit.
 */
public final class BoilerConstants {
    /** C: total capacity of the boiler (litre). */
    @RoboChartType("real") public static final double CAPACITY_C = 1000.0;
    /** M1: minimal limit quantity of water (litre). */
    @RoboChartType("real") public static final double LIMIT_M1 = 100.0;
    /** M2: maximal limit quantity of water (litre). */
    @RoboChartType("real") public static final double LIMIT_M2 = 900.0;
    /** N1: minimal normal quantity of water (litre). */
    @RoboChartType("real") public static final double NORMAL_N1 = 400.0;
    /** N2: maximal normal quantity of water (litre). */
    @RoboChartType("real") public static final double NORMAL_N2 = 600.0;
    /** W: maximal quantity of steam leaving the boiler (litre/sec). */
    @RoboChartType("real") public static final double STEAM_MAX_W = 10.0;
    /** U1: maximal gradient of increase of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_INCREASE_U1 = 1.0;
    /** U2: maximal gradient of decrease of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_DECREASE_U2 = 1.0;
    /** P: nominal capacity of each pump (litre/sec). */
    @RoboChartType("real") public static final double PUMP_CAPACITY_P = 5.0;
    /** Length of one transmission cycle, also the pump start-up latency (sec). */
    @RoboChartType("real") public static final double CYCLE_SECONDS = 5.0;
    /** Number of pumps, and of pump controllers. */
    @RoboChartType("nat") public static final int PUMP_COUNT = 4;
    /** Number of successive transmissions carrying STOP that forces emergency stop. */
    @RoboChartType("nat") public static final int STOP_LIMIT = 3;

    private BoilerConstants() {}
}
