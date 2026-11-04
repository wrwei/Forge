package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * Content of PUMP_CONTROL_STATE(n, b): pump number n (1 to 4) and what its
 * controller observes (SB-DM4, SB-DM9).
 */
public record PumpControlStateData(@RoboChartType("nat") int pump, FlowObservation flow) {
}
