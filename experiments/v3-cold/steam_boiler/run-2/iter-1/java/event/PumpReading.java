package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * Payload of PUMP_STATE(n, b): pump {@code pump} (1 to 4) reports whether it is on.
 *
 * @param pump the pump number, 1 to 4
 * @param running true when the pump is on, false when it is off
 */
public record PumpReading(@RoboChartType("nat") int pump, boolean running) {}
