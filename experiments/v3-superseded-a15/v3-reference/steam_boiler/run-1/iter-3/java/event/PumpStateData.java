package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * Content of PUMP_STATE(n, b): pump number n (1 to 4) and whether it is switched on
 * (SB-DM3, SB-DM9).
 */
public record PumpStateData(@RoboChartType("nat") int pump, boolean switchedOn) {
}
