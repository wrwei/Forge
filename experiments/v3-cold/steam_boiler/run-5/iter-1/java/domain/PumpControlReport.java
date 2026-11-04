package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * Payload of PUMP_CONTROL_STATE(n, b): the controller of pump {@code controlledPump} (1..4)
 * observes {@code observation}.
 */
public record PumpControlReport(@RoboChartType("nat") int controlledPump, FlowObservation observation) {
}
