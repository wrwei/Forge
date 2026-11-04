package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * Messages received by the program (SB-DM9), plus the marker that closes the
 * receive stage of a cycle (SB-V3). Pump numbers run from 1 to 4.
 */
public sealed interface InputEvent {

    /** End of the receive stage: every message of this cycle's transmission has arrived. */
    record TransmissionReceived() implements InputEvent {}

    /** STOP: operator request. */
    record StopRequest() implements InputEvent {}

    /** STEAM_BOILER_WAITING: the plant is ready for the program to begin. */
    record SteamBoilerWaiting() implements InputEvent {}

    /** PHYSICAL_UNITS_READY: answer to PROGRAM_READY. */
    record PhysicalUnitsReady() implements InputEvent {}

    /** PUMP_STATE(n, b). */
    record PumpState(PumpStateData report) implements InputEvent {}

    /** PUMP_CONTROL_STATE(n, b). */
    record PumpControlState(PumpControlStateData report) implements InputEvent {}

    /** LEVEL(v): water-level reading (litre). */
    record Level(@RoboChartType("real") double value) implements InputEvent {}

    /** STEAM(v): steam-rate reading (litre/sec). */
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
