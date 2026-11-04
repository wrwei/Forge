package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * Messages the program receives from the physical units, plus the signal that one cycle's
 * transmission has been received in full. Pumps are numbered 1 to 4.
 */
public sealed interface InputEvent {

    /** The cycle's incoming transmission has been received; the program now analyses it. */
    record TransmissionReceived() implements InputEvent {}

    /** STOP: an operator request; three in successive transmissions force emergency stop. */
    record StopRequest() implements InputEvent {}

    /** STEAM_BOILER_WAITING: the plant is ready for the program to begin. */
    record SteamBoilerWaiting() implements InputEvent {}

    /** PHYSICAL_UNITS_READY: the units' answer to PROGRAM_READY. */
    record PhysicalUnitsReady() implements InputEvent {}

    /** PUMP_STATE(n, b): the on/off state of pump n. */
    record PumpState(PumpReport report) implements InputEvent {}

    /** PUMP_CONTROL_STATE(n, b): whether the controller of pump n sees water flowing. */
    record PumpControlState(PumpControlReport report) implements InputEvent {}

    /** LEVEL(v): the water-level reading, in litres. */
    record Level(@RoboChartType("real") double value) implements InputEvent {}

    /** STEAM(v): the steam-rate reading, in litres per second. */
    record Steam(@RoboChartType("real") double value) implements InputEvent {}

    /** PUMP_REPAIRED(n). */
    record PumpRepaired(@RoboChartType("nat") int pumpNumber) implements InputEvent {}

    /** PUMP_CONTROL_REPAIRED(n). */
    record PumpControlRepaired(@RoboChartType("nat") int pumpNumber) implements InputEvent {}

    /** LEVEL_REPAIRED. */
    record LevelRepaired() implements InputEvent {}

    /** STEAM_REPAIRED. */
    record SteamRepaired() implements InputEvent {}

    /** PUMP_FAILURE_ACKNOWLEDGEMENT(n). */
    record PumpFailureAcknowledgement(@RoboChartType("nat") int pumpNumber) implements InputEvent {}

    /** PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT(n). */
    record PumpControlFailureAcknowledgement(@RoboChartType("nat") int pumpNumber)
            implements InputEvent {}

    /** LEVEL_FAILURE_ACKNOWLEDGEMENT. */
    record LevelFailureAcknowledgement() implements InputEvent {}

    /** STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT. */
    record SteamOutcomeFailureAcknowledgement() implements InputEvent {}
}
