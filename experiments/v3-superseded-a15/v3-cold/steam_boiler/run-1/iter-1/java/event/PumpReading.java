package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/** The on/off state reported for one pump: the payload of PUMP_STATE(n, b). */
public record PumpReading(@RoboChartType("nat") int pumpNumber, PumpPosition position) {
}
