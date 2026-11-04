package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * The content of PUMP_STATE(n, b): the on/off state of pump {@code pumpNumber}, numbered from 1
 * (SB-DM3, SB-DM9).
 */
public record PumpReport(@RoboChartType("nat") int pumpNumber, PumpPosition position) {
}
