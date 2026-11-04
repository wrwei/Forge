package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * Messages the program receives from the physical units (SB-DM9), plus the
 * signal that the receive stage of a cycle is complete (SB-V3).
 */
public sealed interface InputEvent {
    /** All messages of the current transmission have been received; analysis starts. */
    record TransmissionReceived() implements InputEvent {}

    /** STOP: an operator request. */
    record StopRequest() implements InputEvent {}

    /** STEAM_BOILER_WAITING: the plant is ready for the program to begin. */
    record SteamBoilerWaiting() implements InputEvent {}

    /** PHYSICAL_UNITS_READY: the answer to PROGRAM_READY. */
    record PhysicalUnitsReady() implements InputEvent {}

    /** PUMP_STATE(n, b): the on/off state of pump n. */
    record PumpStateReport(PumpReading pumpObservation) implements InputEvent {}

    /** PUMP_CONTROL_STATE(n, b): whether the controller of pump n sees water flowing. */
    record PumpControlStateReport(PumpControlReading flowObservation) implements InputEvent {}

    /** LEVEL(v): the water-level reading, in litres. */
    record LevelReport(@RoboChartType("real") double levelValue) implements InputEvent {}

    /** STEAM(v): the steam-rate reading, in litres per second. */
    record SteamReport(@RoboChartType("real") double steamValue) implements InputEvent {}

    /** PUMP_REPAIRED(n). */
    record PumpRepaired(@RoboChartType("nat") int repairedPump) implements InputEvent {}

    /** PUMP_CONTROL_REPAIRED(n). */
    record PumpControlRepaired(@RoboChartType("nat") int repairedController) implements InputEvent {}

    /** LEVEL_REPAIRED. */
    record LevelRepaired() implements InputEvent {}

    /** STEAM_REPAIRED. */
    record SteamRepaired() implements InputEvent {}

    /** PUMP_FAILURE_ACKNOWLEDGEMENT(n). */
    record PumpFailureAcknowledgement(@RoboChartType("nat") int acknowledgedPump) implements InputEvent {}

    /** PUMP_CONTROL_FAILURE_ACKNOWLEDGEMENT(n). */
    record PumpControlFailureAcknowledgement(@RoboChartType("nat") int acknowledgedController)
            implements InputEvent {}

    /** LEVEL_FAILURE_ACKNOWLEDGEMENT. */
    record LevelFailureAcknowledgement() implements InputEvent {}

    /** STEAM_OUTCOME_FAILURE_ACKNOWLEDGEMENT. */
    record SteamOutcomeFailureAcknowledgement() implements InputEvent {}
}
