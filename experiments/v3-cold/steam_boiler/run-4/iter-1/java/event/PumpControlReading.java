package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * The observation reported by one pump controller (SB-DM4, PUMP_CONTROL_STATE(n, b)).
 *
 * @param controlIndex the number of the pump the controller observes, 1 to 4
 * @param waterFlowing whether water is circulating from that pump into the boiler
 */
public record PumpControlReading(@RoboChartType("nat") int controlIndex, boolean waterFlowing) {}
