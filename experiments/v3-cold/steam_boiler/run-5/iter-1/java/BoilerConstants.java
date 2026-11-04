package steam_boiler;

import steam_boiler.annotation.RoboChartType;

/**
 * Constants of the boiler, the steam exit, the pumps and the control cycle. The requirements
 * name these quantities but give no values; the values below are chosen (see post_codegen.md)
 * so that M1 &lt; N1 &lt; N2 &lt; M2 &lt; C and the four pumps together can outpace the
 * maximal steam rate.
 */
public final class BoilerConstants {

    /** C: total capacity of the boiler, litres. */
    @RoboChartType("real") public static final double CAPACITY_C = 1000.0;

    /** M1: minimal limit quantity of water, litres. */
    @RoboChartType("real") public static final double MIN_LIMIT_M1 = 150.0;

    /** M2: maximal limit quantity of water, litres. */
    @RoboChartType("real") public static final double MAX_LIMIT_M2 = 850.0;

    /** N1: minimal normal quantity of water, litres. */
    @RoboChartType("real") public static final double MIN_NORMAL_N1 = 400.0;

    /** N2: maximal normal quantity of water, litres. */
    @RoboChartType("real") public static final double MAX_NORMAL_N2 = 600.0;

    /** W: maximal quantity of steam leaving the boiler, litres per second. */
    @RoboChartType("real") public static final double MAX_STEAM_W = 25.0;

    /** U1: maximal gradient of increase of the steam rate, litres per second per second. */
    @RoboChartType("real") public static final double STEAM_INCREASE_U1 = 2.0;

    /** U2: maximal gradient of decrease of the steam rate, litres per second per second. */
    @RoboChartType("real") public static final double STEAM_DECREASE_U2 = 2.0;

    /** P: nominal capacity of each pump, litres per second. */
    @RoboChartType("real") public static final double PUMP_CAPACITY_P = 10.0;

    /** Number of pumps (and of pump controllers). */
    @RoboChartType("nat") public static final int PUMP_COUNT = 4;

    /** Length of one control cycle, seconds; also the pump start-up latency. */
    @RoboChartType("real") public static final double CYCLE_SECONDS = 5.0;

    /** Number of successive transmissions carrying STOP that force emergency stop. */
    @RoboChartType("nat") public static final int STOP_LIMIT = 3;

    private BoilerConstants() {
    }
}
