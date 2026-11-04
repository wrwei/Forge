package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * The payload of PUMP_STATE(n, b): pump number n (1 to 4) and its on/off state.
 */
public record PumpReport(@RoboChartType("nat") int pump, PumpSetting setting) {
}
