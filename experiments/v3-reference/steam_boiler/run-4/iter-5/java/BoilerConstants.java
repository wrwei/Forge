package steam_boiler;

import steam_boiler.annotation.RoboChartType;

/**
 * The plant's constants (SB-DM1, SB-DM2, SB-DM3). The requirements name these
 * quantities but give no values; the values below are an example
 * configuration, chosen so that M1 &lt; N1 &lt; N2 &lt; M2 &lt; C and so that one cycle of
 * worst-case evolution from the normal band cannot reach a limit.
 */
public final class BoilerConstants {

    /** C: total capacity of the boiler (litre). */
    @RoboChartType("real") public static final double CAPACITY = 1000.0;

    /** M1: minimal limit quantity of water (litre). */
    @RoboChartType("real") public static final double MIN_LIMIT = 100.0;

    /** M2: maximal limit quantity of water (litre). */
    @RoboChartType("real") public static final double MAX_LIMIT = 900.0;

    /** N1: minimal normal quantity of water (litre). */
    @RoboChartType("real") public static final double MIN_NORMAL = 400.0;

    /** N2: maximal normal quantity of water (litre). */
    @RoboChartType("real") public static final double MAX_NORMAL = 600.0;

    /** W: maximal quantity of steam leaving the boiler (litre/sec). */
    @RoboChartType("real") public static final double MAX_STEAM = 10.0;

    /** U1: maximal gradient of increase of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_INCREASE_GRADIENT = 1.0;

    /** U2: maximal gradient of decrease of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_DECREASE_GRADIENT = 1.0;

    /** P: nominal capacity of each pump (litre/sec). */
    @RoboChartType("real") public static final double PUMP_CAPACITY = 5.0;

    /** Duration of one cycle, which is also a pump's start-up latency (sec). */
    @RoboChartType("real") public static final double CYCLE_DURATION = 5.0;

    /** Number of pumps (and of pump controllers). */
    @RoboChartType("nat") public static final int PUMP_COUNT = 4;

    /** Number of successive transmissions carrying STOP that force emergency stop. */
    @RoboChartType("nat") public static final int STOP_LIMIT = 3;

    private BoilerConstants() {
    }
}
