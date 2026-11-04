package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * Payload of PUMP_CONTROL_STATE(n, b): what the controller of pump {@code controlledPump} sees.
 *
 * @param controlledPump the number of the pump the controller watches
 * @param flow whether water flows from that pump into the boiler
 */
public record PumpControlReport(@RoboChartType("nat") int controlledPump, WaterFlow flow) {
}
