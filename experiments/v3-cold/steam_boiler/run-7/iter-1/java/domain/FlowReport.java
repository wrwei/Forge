package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * The payload of PUMP_CONTROL_STATE(n, b): pump number n (1 to 4) and whether
 * its controller sees water flowing.
 */
public record FlowReport(@RoboChartType("nat") int pump, WaterFlow flow) {
}
