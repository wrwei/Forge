package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * Payload of PUMP_STATE(n, b): pump {@code reportedPump} (1 to 4) is switched on or off.
 *
 * @param reportedPump the pump number
 * @param switchedOn whether the pump reports itself on
 */
public record PumpReport(@RoboChartType("nat") int reportedPump, boolean switchedOn) {
}
