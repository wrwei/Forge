package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;
import steam_boiler.domain.PumpControlReading;
import steam_boiler.domain.PumpReading;

/**
 * Messages received from the physical units (SB-DM9), plus the marker that the
 * reception stage of a cycle is complete (SB-V3).
 */
public sealed interface InputEvent {
    /** STOP: operator request. */
    record StopRequest() implements InputEvent {}
    /** STEAM_BOILER_WAITING: the plant is ready for the program to begin. */
    record SteamBoilerWaiting() implements InputEvent {}
    /** PHYSICAL_UNITS_READY: answer to PROGRAM_READY. */
    record PhysicalUnitsReady() implements InputEvent {}
    /** PUMP_STATE(n, b). */
    record PumpState(PumpReading reading) implements InputEvent {}
    /** PUMP_CONTROL_STATE(n, b). */
    record PumpControlState(PumpControlReading reading) implements InputEvent {}
    /** LEVEL(v): water-level reading in litres. */
    record Level(@RoboChartType("real") double levelValue) implements InputEvent {}
    /** STEAM(v): steam-rate reading in litres per second. */
    record Steam(@RoboChartType("real") double steamValue) implements InputEvent {}
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
    /** All messages of the current transmission have been received; the program analyses and replies. */
    record TransmissionComplete() implements InputEvent {}
}
