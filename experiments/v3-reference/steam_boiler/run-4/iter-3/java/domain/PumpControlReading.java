package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * The content of a PUMP_CONTROL_STATE(n, b) message (SB-DM4, SB-DM9): the
 * controller of pump n reports whether water is flowing from that pump.
 *
 * @param controlledPump the pump whose controller reports, 1 to 4
 * @param observation    what the controller observes
 */
public record PumpControlReading(@RoboChartType("nat") int controlledPump, FlowObservation observation) {
}
