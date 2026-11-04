package steam_boiler.controller;

import steam_boiler.annotation.RoboChartType;

/**
 * The constants that characterise the boiler, its steam exit, its pumps and the control cycle.
 * The requirements name C, M1, M2, N1, N2, W, U1, U2 and P without giving values; the values here
 * are chosen to satisfy M1 &lt; N1 &lt; N2 &lt; M2 &lt; C and are integers so that the model needs
 * no rounding.
 */
public final class BoilerConstants {

    /** C: total capacity of the boiler (litres). */
    @RoboChartType("real") public static final double CAPACITY_C = 1000.0;

    /** M1: minimal limit quantity of water (litres). */
    @RoboChartType("real") public static final double LIMIT_M1 = 100.0;

    /** M2: maximal limit quantity of water (litres). */
    @RoboChartType("real") public static final double LIMIT_M2 = 900.0;

    /** N1: minimal normal quantity of water (litres). */
    @RoboChartType("real") public static final double NORMAL_N1 = 400.0;

    /** N2: maximal normal quantity of water (litres). */
    @RoboChartType("real") public static final double NORMAL_N2 = 600.0;

    /** W: maximal quantity of steam leaving the boiler (litres/sec). */
    @RoboChartType("real") public static final double STEAM_MAX_W = 25.0;

    /** U1: maximal gradient of increase of the steam rate (litres/sec/sec). */
    @RoboChartType("real") public static final double STEAM_RISE_U1 = 4.0;

    /** U2: maximal gradient of decrease of the steam rate (litres/sec/sec). */
    @RoboChartType("real") public static final double STEAM_FALL_U2 = 6.0;

    /** P: nominal capacity of each pump (litres/sec). */
    @RoboChartType("real") public static final double PUMP_CAPACITY_P = 10.0;

    /** The number of pumps (and of pump controllers). */
    @RoboChartType("nat") public static final int PUMP_COUNT = 4;

    /** The length of one control cycle, and the pump start-up latency (seconds). */
    @RoboChartType("real") public static final double CYCLE_SECONDS = 5.0;

    /** The number of successive transmissions carrying STOP that force emergency stop. */
    @RoboChartType("nat") public static final int STOP_LIMIT = 3;

    private BoilerConstants() {}
}
