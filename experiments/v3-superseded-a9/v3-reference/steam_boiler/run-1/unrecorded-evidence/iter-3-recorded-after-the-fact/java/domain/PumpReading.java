package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * The content of a PUMP_STATE(n, b) message: pump number {@code pump} (1 to 4) and its reported
 * on/off position.
 */
public record PumpReading(@RoboChartType("nat") int pump, PumpPosition position) {}
