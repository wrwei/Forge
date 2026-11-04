package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.FlowReport;
import steam_boiler.domain.PumpReport;

/**
 * Messages received from the physical units, one record per message, and the
 * cycle trigger that starts the analysis stage once a transmission is in.
 */
public sealed interface InputEvent {

    /** The incoming transmission of the current cycle has been received. */
    record TransmissionReceived() implements InputEvent {}

    /** STOP: operator request. */
    record StopMessage() implements InputEvent {}

    /** STEAM_BOILER_WAITING: the plant is ready for the program to begin. */
    record SteamBoilerWaiting() implements InputEvent {}

    /** PHYSICAL_UNITS_READY: answer to PROGRAM_READY. */
    record PhysicalUnitsReady() implements InputEvent {}

    /** PUMP_STATE(n, b). */
    record PumpState(PumpReport report) implements InputEvent {}

    /** PUMP_CONTROL_STATE(n, b). */
    record PumpControlState(FlowReport report) implements InputEvent {}

    /** LEVEL(v): the water-level reading, in litres. */
    record Level(@RoboChartType("real") double value) implements InputEvent {}

    /** STEAM(v): the steam-rate reading, in litres per second. */
    record Steam(@RoboChartType("real") double value) implements InputEvent {}

    /** PUMP_REPAIRED(n). */
    record PumpRepaired(@RoboChartType("nat") int pump) implements InputEvent {}

    /** PUMP_CONTROL_REPAIRED(n). */
    record PumpControlRepaired(@RoboChartType("nat") int pump) implements InputEvent {}

    /** LEVEL_REPAIRED. */
    record LevelRepaired() implements InputEvent {}

    /** STEAM_REPAIRED. */
    record SteamRepaired() implements InputEvent {}

    /** PUMP_FAILURE_ACKNOWLEDGEMENT(n). */
    record PumpFailureAcknowledgement(@RoboChartType("nat") int pump) implements InputEvent {}

    /** PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT(n). */
    record PumpControlFailureAcknowledgement(@RoboChartType("nat") int pump) implements InputEvent {}

    /** LEVEL_FAILURE_ACKNOWLEDGEMENT. */
    record LevelFailureAcknowledgement() implements InputEvent {}

    /** STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT. */
    record SteamOutcomeFailureAcknowledgement() implements InputEvent {}
}
