package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * Payload of PUMP_CONTROL_STATE(n, b): the controller of pump {@code pump} reports
 * one of its two observations, water flows or water does not flow.
 *
 * @param pump the pump number, 1 to 4
 * @param flowing true when water circulates from the pump into the boiler
 */
public record FlowReading(@RoboChartType("nat") int pump, boolean flowing) {}
