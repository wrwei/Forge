package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * Content of a PUMP_STATE(n, b) message: the pump number n (1 to 4) and its
 * reported on/off state b.
 */
public record PumpStateReport(@RoboChartType("nat") int pumpNumber, PumpPosition reportedPosition) {
}
