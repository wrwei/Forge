package steam_boiler;

import steam_boiler.annotation.RoboChartType;

/**
 * Constant characteristics of the boiler, the steam exit and the pumps
 * (SB-DM1, SB-DM2, SB-DM3) and of the cycle (SB-V3, SB-Beh23). The
 * requirements name these quantities without fixing their values; the
 * values below are chosen to respect M1 &lt; N1 &lt; N2 &lt; M2 &lt; C.
 */
public final class BoilerConstants {
    /** C: total capacity of the boiler (litre). */
    @RoboChartType("real") public static final double CAPACITY = 1000.0;
    /** M1: minimal limit quantity of water (litre). */
    @RoboChartType("real") public static final double M1 = 125.0;
    /** M2: maximal limit quantity of water (litre). */
    @RoboChartType("real") public static final double M2 = 800.0;
    /** N1: minimal normal quantity of water (litre). */
    @RoboChartType("real") public static final double N1 = 400.0;
    /** N2: maximal normal quantity of water (litre). */
    @RoboChartType("real") public static final double N2 = 600.0;
    /** W: maximal quantity of steam leaving the boiler (litre/sec). */
    @RoboChartType("real") public static final double MAX_STEAM_RATE = 25.0;
    /** U1: maximal gradient of increase of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double MAX_STEAM_INCREASE = 2.0;
    /** U2: maximal gradient of decrease of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double MAX_STEAM_DECREASE = 2.0;
    /** P: nominal capacity of each pump (litre/sec). */
    @RoboChartType("real") public static final double PUMP_CAPACITY = 10.0;
    /** Length of one cycle, which is also the pump start-up latency (sec). */
    @RoboChartType("real") public static final double CYCLE_SECONDS = 5.0;
    /** Number of pumps, and of pump controllers. */
    @RoboChartType("nat") public static final int PUMP_COUNT = 4;
    /** Number of successive transmissions carrying STOP that force emergency stop. */
    @RoboChartType("nat") public static final int STOP_LIMIT = 3;

    private BoilerConstants() {
    }
}
