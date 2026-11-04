package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * Payload of PUMP_STATE(n, b): pump {@code reportedPump} (1..4) is on ({@code pumpOn}) or off.
 */
public record PumpStateReport(@RoboChartType("nat") int reportedPump, boolean pumpOn) {
}
