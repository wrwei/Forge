package steam_boiler.data;

import steam_boiler.annotation.RoboChartType;

/**
 * The content of a PUMP_CONTROL_STATE(n, b) message: pump number n (1 to 4) and whether its
 * controller sees water flowing (SB-DM4, SB-DM9).
 */
public record PumpControlReading(@RoboChartType("nat") int controlledPump, WaterFlow flow) {
}
