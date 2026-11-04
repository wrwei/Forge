package steam_boiler.data;

import steam_boiler.annotation.RoboChartType;

/**
 * The on/off state reported by pump {@code pump} (1 to 4), the content of PUMP_STATE(n, b).
 *
 * @param pump the pump number
 * @param open whether the pump is on
 */
public record PumpReading(@RoboChartType("nat") int pump, boolean open) {
}
