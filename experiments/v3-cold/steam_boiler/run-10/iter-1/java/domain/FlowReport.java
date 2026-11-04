package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * The content of a PUMP_CONTROL_STATE(n, b) message: the controller of pump {@code controllerIndex}
 * sees water flowing or not (SB-DM4, SB-DM9). Pump controllers are numbered 0 to 3, like their pumps.
 *
 * @param controllerIndex the number of the pump the controller observes
 * @param flow the controller's observation
 */
public record FlowReport(@RoboChartType("nat") int controllerIndex, WaterFlow flow) {
}
