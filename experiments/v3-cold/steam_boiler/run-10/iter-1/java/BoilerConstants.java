package steam_boiler;

import steam_boiler.annotation.RoboChartType;

/**
 * Constants of the boiler, the steam exit, the pumps and the cycle (SB-DM1, SB-DM2, SB-DM3, SB-V3).
 * The requirements name these quantities but fix only the pump count, the cycle length and the
 * number of successive STOP messages; the other values are chosen for this implementation and
 * satisfy M1 &lt; N1 &lt; N2 &lt; M2 &lt; C.
 */
public final class BoilerConstants {

    /** C: total capacity of the boiler, litres (SB-DM1). */
    @RoboChartType("real") public static final double CAPACITY_C = 1000.0;

    /** M1: minimal limit, litres (SB-DM1). */
    @RoboChartType("real") public static final double LEVEL_M1 = 100.0;

    /** M2: maximal limit, litres (SB-DM1). */
    @RoboChartType("real") public static final double LEVEL_M2 = 900.0;

    /** N1: minimal normal level, litres (SB-DM1). */
    @RoboChartType("real") public static final double LEVEL_N1 = 400.0;

    /** N2: maximal normal level, litres (SB-DM1). */
    @RoboChartType("real") public static final double LEVEL_N2 = 600.0;

    /** W: maximal quantity of steam leaving the boiler, litres per second (SB-DM2). */
    @RoboChartType("real") public static final double STEAM_MAX_W = 10.0;

    /** U1: maximal increase gradient of the steam rate, litres per second per second (SB-DM2). */
    @RoboChartType("real") public static final double STEAM_RISE_U1 = 1.0;

    /** U2: maximal decrease gradient of the steam rate, litres per second per second (SB-DM2). */
    @RoboChartType("real") public static final double STEAM_FALL_U2 = 1.0;

    /** The steam rate that initialization requires: none at all, litres per second (SB-Beh3). */
    @RoboChartType("real") public static final double STEAM_AT_REST = 0.0;

    /** P: nominal capacity of each pump, litres per second (SB-DM3). */
    @RoboChartType("real") public static final double PUMP_CAPACITY_P = 5.0;

    /** Number of pumps, and of pump controllers (SB-V1, SB-DM3). */
    @RoboChartType("nat") public static final int PUMP_COUNT = 4;

    /** Length of one cycle, seconds; also the start-up latency of a pump (SB-V3, SB-DM3). */
    @RoboChartType("real") public static final double CYCLE_SECONDS = 5.0;

    /** Successive transmissions carrying STOP that force emergency stop (SB-DM9, SB-Beh23). */
    @RoboChartType("nat") public static final int STOP_REPETITIONS = 3;

    private BoilerConstants() {
    }
}
