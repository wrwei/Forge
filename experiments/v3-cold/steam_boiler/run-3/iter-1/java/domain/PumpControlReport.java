package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * Content of a PUMP_CONTROL_STATE(n, b) message: the number n (1 to 4) of the
 * pump whose controller reports, and whether that controller sees water flowing.
 */
public record PumpControlReport(@RoboChartType("nat") int controlledPump, FlowObservation observedFlow) {
}
