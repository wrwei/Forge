package steam_boiler.controller;

import steam_boiler.annotation.RoboChartType;

/**
 * The constant characteristics of the plant (SB-DM1, SB-DM2, SB-DM3) and of the protocol
 * (SB-V3, SB-Beh23). The requirements name these constants but give no values; the values
 * below are chosen so that M1 &lt; N1 &lt; N2 &lt; M2 &lt; C and so that one cycle of
 * worst-case steam (5 W) or pump (5 * 4 * P) evolution cannot carry the level from a normal
 * bound to a limit.
 */
public final class SteamBoilerConstants {

    /** C: total capacity of the boiler, in litres. */
    @RoboChartType("real") public static final double CAPACITY = 1000.0;

    /** M1: minimal limit quantity of water, in litres. */
    @RoboChartType("real") public static final double MIN_LIMIT = 150.0;

    /** M2: maximal limit quantity of water, in litres. */
    @RoboChartType("real") public static final double MAX_LIMIT = 850.0;

    /** N1: minimal normal quantity of water, in litres. */
    @RoboChartType("real") public static final double MIN_NORMAL = 400.0;

    /** N2: maximal normal quantity of water, in litres. */
    @RoboChartType("real") public static final double MAX_NORMAL = 600.0;

    /** W: maximal quantity of steam leaving the boiler, in litres per second. */
    @RoboChartType("real") public static final double MAX_STEAM_RATE = 25.0;

    /** U1: maximal gradient of increase of the steam rate, in litres per second per second. */
    @RoboChartType("real") public static final double MAX_STEAM_INCREASE = 2.0;

    /** U2: maximal gradient of decrease of the steam rate, in litres per second per second. */
    @RoboChartType("real") public static final double MAX_STEAM_DECREASE = 2.0;

    /** P: nominal capacity of each pump, in litres per second. */
    @RoboChartType("real") public static final double PUMP_CAPACITY = 10.0;

    /** Number of pumps (and of pump controllers). */
    @RoboChartType("nat") public static final int PUMP_COUNT = 4;

    /** Length of one cycle, in seconds; also the pump start-up latency. */
    @RoboChartType("real") public static final double CYCLE_SECONDS = 5.0;

    /** Number of successive transmissions carrying STOP that demand emergency stop. */
    @RoboChartType("nat") public static final int STOP_REPEAT_LIMIT = 3;

    private SteamBoilerConstants() {
    }
}
