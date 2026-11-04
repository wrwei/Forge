package steam_boiler.data;

import steam_boiler.annotation.RoboChartType;

/**
 * The content of a PUMP_STATE(n, b) message: pump number n (1 to 4) and its on/off state
 * (SB-DM3, SB-DM9).
 */
public record PumpStateReading(@RoboChartType("nat") int pumpNumber, PumpPosition position) {
}
