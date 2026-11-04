package steam_boiler.controller;

import steam_boiler.annotation.RoboChartType;

/**
 * The constants characterising the boiler, the steam exit and the pumps.
 * The requirements name these quantities but give no values; the values
 * below are chosen so that M1 &lt; N1 &lt; N2 &lt; M2 &lt; C and so that the
 * four pumps together can exceed the maximal steam rate.
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
    @RoboChartType("real") public static final double MAX_STEAM_W = 25.0;

    /** U1: maximal gradient of increase of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_RISE_U1 = 2.0;

    /** U2: maximal gradient of decrease of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_FALL_U2 = 2.0;

    /** P: nominal capacity of each pump (litre/sec). */
    @RoboChartType("real") public static final double PUMP_CAPACITY_P = 10.0;

    /** Number of pumps (and of pump controllers). */
    @RoboChartType("nat") public static final int PUMP_COUNT = 4;

    /** Length of one cycle (sec). */
    @RoboChartType("real") public static final double CYCLE_SECONDS = 5.0;

    /** Successive transmissions carrying STOP that force emergency stop. */
    @RoboChartType("nat") public static final int STOP_LIMIT = 3;

    private BoilerConstants() {
    }
}
