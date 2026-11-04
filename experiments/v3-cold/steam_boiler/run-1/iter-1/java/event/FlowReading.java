package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * The content of PUMP_CONTROL_STATE(n, b): the controller of pump {@code pump}
 * observes that water flows ({@code flowing}) or does not flow.
 *
 * @param pump the pump number, 0 to 3
 * @param flowing whether water is observed circulating from the pump
 */
public record FlowReading(@RoboChartType("nat") int pump, boolean flowing) {}
