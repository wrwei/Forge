package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * The content of PUMP_STATE(n, b): the on/off position reported for pump n.
 *
 * @param pumpIndex the pump number, 1 to 4
 * @param position  the reported position
 */
public record PumpReading(@RoboChartType("nat") int pumpIndex, PumpPosition position) {
}
