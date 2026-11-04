package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * The content of PUMP_CONTROL_STATE(n, b): whether the controller of pump {@code controllerNumber},
 * numbered from 1, sees water flowing (SB-DM4, SB-DM9).
 */
public record FlowReport(@RoboChartType("nat") int controllerNumber, WaterFlow flow) {
}
