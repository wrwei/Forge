package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * Payload of PUMP_CONTROL_STATE(n, b): the controller of pump {@code controlPump}
 * reports whether water flows (SB-DM4, SB-DM9).
 */
public record PumpControlReport(@RoboChartType("nat") int controlPump, WaterFlow flow) {
}
