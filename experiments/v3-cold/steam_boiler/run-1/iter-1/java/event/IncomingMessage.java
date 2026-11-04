package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * The messages the program receives from the physical units. A transmission is
 * the set of these messages received in one cycle.
 */
public sealed interface IncomingMessage {
    /** STOP: an operator request. */
    record StopRequest() implements IncomingMessage {}

    /** STEAM_BOILER_WAITING: the plant is ready for the program to begin. */
    record SteamBoilerWaiting() implements IncomingMessage {}

    /** PHYSICAL_UNITS_READY: the answer to PROGRAM_READY. */
    record PhysicalUnitsReady() implements IncomingMessage {}

    /** PUMP_STATE(n, b): the on/off state of pump n. */
    record PumpState(PumpReading reading) implements IncomingMessage {}

    /** PUMP_CONTROL_STATE(n, b): whether the controller of pump n sees water flowing. */
    record PumpControlState(FlowReading reading) implements IncomingMessage {}

    /** LEVEL(v): the water-level reading, in litres. */
    record Level(@RoboChartType("real") double value) implements IncomingMessage {}

    /** STEAM(v): the steam-rate reading, in litres per second. */
    record Steam(@RoboChartType("real") double value) implements IncomingMessage {}

    /** PUMP_REPAIRED(n). */
    record PumpRepaired(@RoboChartType("nat") int pump) implements IncomingMessage {}

    /** PUMP_CONTROL_REPAIRED(n). */
    record PumpControlRepaired(@RoboChartType("nat") int pump) implements IncomingMessage {}

    /** LEVEL_REPAIRED. */
    record LevelRepaired() implements IncomingMessage {}

    /** STEAM_REPAIRED. */
    record SteamRepaired() implements IncomingMessage {}

    /** PUMP_FAILURE_ACKNOWLEDGEMENT(n). */
    record PumpFailureAcknowledgement(@RoboChartType("nat") int pump) implements IncomingMessage {}

    /** PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT(n). */
    record PumpControlFailureAcknowledgement(@RoboChartType("nat") int pump) implements IncomingMessage {}

    /** LEVEL_FAILURE_ACKNOWLEDGEMENT. */
    record LevelFailureAcknowledgement() implements IncomingMessage {}

    /** STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT. */
    record SteamOutcomeFailureAcknowledgement() implements IncomingMessage {}
}
