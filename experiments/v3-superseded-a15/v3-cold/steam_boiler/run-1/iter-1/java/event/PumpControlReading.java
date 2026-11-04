package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/** The flow observed by one pump controller: the payload of PUMP_CONTROL_STATE(n, b). */
public record PumpControlReading(@RoboChartType("nat") int controlledPump, WaterFlow flow) {
}
