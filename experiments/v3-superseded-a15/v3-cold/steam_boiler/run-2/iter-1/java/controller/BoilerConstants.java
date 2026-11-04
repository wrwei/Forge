package steam_boiler.controller;

import steam_boiler.annotation.RoboChartType;

/**
 * Constants of the boiler (SB-DM1), of the steam exit (SB-DM2), of the pumps (SB-DM3) and of the
 * control cycle (SB-V3, SB-V10).
 *
 * <p>The requirements name the boiler, steam and pump constants without giving their values. The
 * values below are chosen to satisfy the relations the requirements state: M1 &lt; N1 &lt; N2 &lt;
 * M2 &lt; C; M1 is at least the steam that can escape in five seconds at the maximal rate W; and
 * C - M2 is at least the water all four pumps can supply in five seconds.
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
    @RoboChartType("real") public static final double STEAM_MAX_W = 15.0;

    /** U1: maximal gradient of increase of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_RISE_U1 = 1.0;

    /** U2: maximal gradient of decrease of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_FALL_U2 = 1.0;

    /** P: nominal capacity of each pump (litre/sec). */
    @RoboChartType("real") public static final double PUMP_CAPACITY_P = 5.0;

    /** Number of pumps, and of pump controllers. */
    @RoboChartType("nat") public static final int PUMP_COUNT = 4;

    /** Length of one control cycle, and the pump start-up latency (sec). */
    @RoboChartType("real") public static final double CYCLE_SECONDS = 5.0;

    /** Number of successive transmissions carrying STOP that force emergency stop. */
    @RoboChartType("nat") public static final int STOP_LIMIT = 3;

    private BoilerConstants() {
    }
}
