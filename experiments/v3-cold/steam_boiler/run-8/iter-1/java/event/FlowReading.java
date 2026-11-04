package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * Content of the PUMP_CONTROL_STATE(n, b) message (SB-DM4, SB-DM9): the
 * controller of pump {@code controllerIndex} (1 to 4) reports whether water
 * flows from that pump into the boiler.
 *
 * @param controllerIndex number of the pump whose controller reports, 1 to 4
 * @param waterFlowing    {@code true} if water flows
 */
public record FlowReading(@RoboChartType("nat") int controllerIndex, boolean waterFlowing) {
}
