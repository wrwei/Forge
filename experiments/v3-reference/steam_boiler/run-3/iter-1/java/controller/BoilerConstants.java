package steam_boiler.controller;

import steam_boiler.annotation.RoboChartType;

/**
 * Constants of the steam boiler (SB-DM1, SB-DM2, SB-DM3) and of the control cycle
 * (SB-V3, SB-V10). The requirements name the quantities but give no values; the
 * values below are chosen to satisfy M1 &lt; N1 &lt; N2 &lt; M2 &lt; C.
 */
public final class BoilerConstants {

    /** C: total capacity of the boiler, litres. */
    @RoboChartType("real") public static final double CAPACITY_C = 1000.0;

    /** M1: minimal limit quantity of water, litres. */
    @RoboChartType("real") public static final double LIMIT_MIN_M1 = 150.0;

    /** M2: maximal limit quantity of water, litres. */
    @RoboChartType("real") public static final double LIMIT_MAX_M2 = 850.0;

    /** N1: minimal normal quantity of water, litres. */
    @RoboChartType("real") public static final double NORMAL_MIN_N1 = 400.0;

    /** N2: maximal normal quantity of water, litres. */
    @RoboChartType("real") public static final double NORMAL_MAX_N2 = 600.0;

    /** W: maximal quantity of steam leaving the boiler, litres per second. */
    @RoboChartType("real") public static final double STEAM_MAX_W = 25.0;

    /** U1: maximal gradient of increase of the steam rate, litres per second per second. */
    @RoboChartType("real") public static final double STEAM_INCREASE_MAX_U1 = 4.0;

    /** U2: maximal gradient of decrease of the steam rate, litres per second per second. */
    @RoboChartType("real") public static final double STEAM_DECREASE_MAX_U2 = 4.0;

    /** P: nominal capacity of each pump, litres per second. */
    @RoboChartType("real") public static final double PUMP_CAPACITY_P = 10.0;

    /** Number of pumps (and of pump controllers); pumps are numbered 1 to 4. */
    @RoboChartType("nat") public static final int PUMP_COUNT = 4;

    /** Pressure-balancing time after a pump is switched on, seconds (SB-Beh25). */
    @RoboChartType("real") public static final double PUMP_START_LATENCY_SECONDS = 5.0;

    /** Length of one control cycle, seconds (SB-V3). */
    @RoboChartType("real") public static final double CYCLE_SECONDS = 5.0;

    /** Number of successive transmissions carrying STOP that demand emergency stop. */
    @RoboChartType("nat") public static final int STOP_REPETITIONS = 3;

    private BoilerConstants() {
    }
}
