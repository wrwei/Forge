package steam_boiler.domain;

import steam_boiler.annotation.RoboChartType;

/**
 * The content of PUMP_CONTROL_STATE(n, b): whether the controller of pump n
 * sees water flowing.
 *
 * @param controllerIndex the number of the pump the controller watches, 1 to 4
 * @param observation     the reported observation
 */
public record PumpControlReading(@RoboChartType("nat") int controllerIndex, WaterFlow observation) {
}
