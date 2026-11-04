package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/**
 * Messages sent by the program to the physical units (SB-DM8). Pump numbers
 * run from 1 to 4.
 */
public sealed interface OutputEvent {

    /** MODE(m): the current operating mode. */
    record ModeMessage(ModeValue value) implements OutputEvent {}

    /** PROGRAM_READY. */
    record ProgramReady() implements OutputEvent {}

    /** VALVE: request to open, and later to close, the evacuation valve. */
    record Valve() implements OutputEvent {}

    /** OPEN_PUMP(n). */
    record OpenPump(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** CLOSE_PUMP(n). */
    record ClosePump(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** PUMP_FAILURE_DETECTION(n). */
    record PumpFailureDetection(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** PUMP_CONTROL_FAILURE_DETECTION(n). */
    record PumpControlFailureDetection(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** LEVEL_FAILURE_DETECTION. */
    record LevelFailureDetection() implements OutputEvent {}

    /** STEAM_FAILURE_DETECTION. */
    record SteamFailureDetection() implements OutputEvent {}

    /** PUMP_REPAIRED_ACKNOWLEDGEMENT(n). */
    record PumpRepairedAcknowledgement(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** PUMP_CONTROL_REPAIRED_ACKNOWLEDGEMENT(n). */
    record PumpControlRepairedAcknowledgement(@RoboChartType("nat") int pump) implements OutputEvent {}

    /** LEVEL_REPAIRED_ACKNOWLEDGEMENT. */
    record LevelRepairedAcknowledgement() implements OutputEvent {}

    /** STEAM_REPAIRED_ACKNOWLEDGEMENT. */
    record SteamRepairedAcknowledgement() implements OutputEvent {}
}
