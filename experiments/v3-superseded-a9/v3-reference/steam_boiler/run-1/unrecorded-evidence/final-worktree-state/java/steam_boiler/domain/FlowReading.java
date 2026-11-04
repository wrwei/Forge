package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * The content of a PUMP_CONTROL_STATE(n, b) message: the controller of pump {@code pump} (1 to 4)
 * and what it observes about the water flow.
 */
public record FlowReading(@RoboChartType("nat") int pump, FlowObservation flow) {}
