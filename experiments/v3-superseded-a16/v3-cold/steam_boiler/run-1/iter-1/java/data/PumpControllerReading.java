package steam_boiler.data;

import steam_boiler.annotation.RoboChartType;

/**
 * The observation of the controller of pump {@code pump} (1 to 4): water flows, or water
 * does not flow. The content of PUMP_CONTROL_STATE(n, b).
 *
 * @param pump the pump number
 * @param flowing whether water circulates from the pump into the boiler
 */
public record PumpControllerReading(@RoboChartType("nat") int pump, boolean flowing) {
}
