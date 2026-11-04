package steam_boiler.controller;

import steam_boiler.annotation.RoboChartType;

/**
 * Plant constants (SB-DM1, SB-DM2, SB-DM3) and cycle constants (SB-V3, SB-DM9).
 * The requirements name these quantities but give no values; the values below
 * are chosen to satisfy M1 &lt; N1 &lt; N2 &lt; M2 &lt; C.
 */
public final class BoilerConstants {
    /** C: total capacity of the boiler (litre). */
    @RoboChartType("real") public static final double CAPACITY_C = 1000.0;
    /** M1: minimal limit (litre). */
    @RoboChartType("real") public static final double MIN_LIMIT_M1 = 100.0;
    /** M2: maximal limit (litre). */
    @RoboChartType("real") public static final double MAX_LIMIT_M2 = 900.0;
    /** N1: minimal normal level (litre). */
    @RoboChartType("real") public static final double MIN_NORMAL_N1 = 400.0;
    /** N2: maximal normal level (litre). */
    @RoboChartType("real") public static final double MAX_NORMAL_N2 = 600.0;
    /** W: maximal quantity of steam leaving the boiler (litre/sec). */
    @RoboChartType("real") public static final double MAX_STEAM_W = 25.0;
    /** U1: maximal gradient of increase of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_INCREASE_U1 = 2.0;
    /** U2: maximal gradient of decrease of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_DECREASE_U2 = 2.0;
    /** P: nominal capacity of each pump (litre/sec). */
    @RoboChartType("real") public static final double PUMP_CAPACITY_P = 10.0;
    /** Number of pumps (and of pump controllers). */
    @RoboChartType("nat") public static final int PUMP_COUNT = 4;
    /** Length of one cycle (sec). */
    @RoboChartType("real") public static final double CYCLE_SECONDS = 5.0;
    /** Number of successive transmissions carrying STOP that force emergency stop. */
    @RoboChartType("nat") public static final int STOP_REPETITIONS = 3;

    private BoilerConstants() {
    }
}
