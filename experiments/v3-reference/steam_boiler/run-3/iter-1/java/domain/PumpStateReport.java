package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * Payload of PUMP_STATE(n, b): pump {@code statePump} reports its on/off state
 * (SB-DM3, SB-DM9).
 */
public record PumpStateReport(@RoboChartType("nat") int statePump, PumpPosition position) {
}
