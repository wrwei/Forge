package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/** The on/off state reported for pump {@code pumpNumber} (SB-DM3). */
public record PumpReading(@RoboChartType("nat") int pumpNumber, boolean switchedOn) {
}
