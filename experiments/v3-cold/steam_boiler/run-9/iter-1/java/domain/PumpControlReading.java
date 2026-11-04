package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/** The observation of the controller of pump {@code controlledPump}: water flows or not (SB-DM4). */
public record PumpControlReading(@RoboChartType("nat") int controlledPump, boolean waterFlowing) {
}
