package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * The on/off state reported for one pump (SB-DM3, PUMP_STATE(n, b)).
 *
 * @param pumpIndex the pump number, 1 to 4
 * @param pumpOn    whether the pump reports itself on
 */
public record PumpReading(@RoboChartType("nat") int pumpIndex, boolean pumpOn) {}
