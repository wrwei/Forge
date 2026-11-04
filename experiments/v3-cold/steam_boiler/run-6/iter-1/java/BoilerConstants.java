package steam_boiler;

import steam_boiler.annotation.RoboChartType;

/**
 * Constant characteristics of the boiler, the steam exit, the pumps and the
 * communication rhythm. The requirements name these quantities but give no
 * values; the values below are chosen so that MIN_LIMIT &lt; MIN_NORMAL &lt;
 * MAX_NORMAL &lt; MAX_LIMIT &lt; CAPACITY and the four pumps together can
 * out-supply the maximal steam rate.
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
    @RoboChartType("real") public static final double MAX_STEAM = 20.0;

    /** U1: maximal gradient of increase of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_RISE_GRADIENT = 2.0;

    /** U2: maximal gradient of decrease of the steam rate (litre/sec/sec). */
    @RoboChartType("real") public static final double STEAM_FALL_GRADIENT = 2.0;

    /** P: nominal capacity of one pump (litre/sec). */
    @RoboChartType("real") public static final double PUMP_CAPACITY = 10.0;

    /** Length of one communication cycle (sec). */
    @RoboChartType("real") public static final double CYCLE_DURATION = 5.0;

    /** Number of pumps (and of pump controllers). */
    @RoboChartType("nat") public static final int PUMP_COUNT = 4;

    /** Number of successive transmissions carrying STOP that force emergency stop. */
    @RoboChartType("nat") public static final int STOP_REPETITIONS = 3;

    private BoilerConstants() {
    }
}
