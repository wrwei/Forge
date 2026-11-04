package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * The content of PUMP_STATE(n, b): pump {@code pump} is reported on ({@code open}) or off.
 *
 * @param pump the pump number, 0 to 3
 * @param open whether the pump reports itself on
 */
public record PumpReading(@RoboChartType("nat") int pump, boolean open) {}
