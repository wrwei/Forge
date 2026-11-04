package steam_boiler.event;

import steam_boiler.annotation.RoboChartType;

/** Messages the program sends to the physical units (SB-DM8). */
public sealed interface OutputEvent {

    /** MODE(m): the current operating mode, sent on every cycle. */
    record ModeAnnouncement(ModeValue reportedMode) implements OutputEvent {}

    /** PROGRAM_READY. */
    record ProgramReady() implements OutputEvent {}

    /** VALVE: request opening, or later closure, of the evacuation valve. */
    record Valve() implements OutputEvent {}

    /** OPEN_PUMP(n). */
    record OpenPump(@RoboChartType("nat") int pumpNumber) implements OutputEvent {}

    /** CLOSE_PUMP(n). */
    record ClosePump(@RoboChartType("nat") int pumpNumber) implements OutputEvent {}

    /** PUMP_FAILURE_DETECTION(n). */
    record PumpFailureDetection(@RoboChartType("nat") int pumpNumber) implements OutputEvent {}

    /** PUMP_CONTROL_FAILURE_DETECTION(n). */
    record PumpControlFailureDetection(@RoboChartType("nat") int pumpNumber) implements OutputEvent {}

    /** LEVEL_FAILURE_DETECTION. */
    record LevelFailureDetection() implements OutputEvent {}

    /** STEAM_FAILURE_DETECTION. */
    record SteamFailureDetection() implements OutputEvent {}

    /** PUMP_REPAIRED_ACKNOWLEDGEMENT(n). */
    record PumpRepairedAcknowledgement(@RoboChartType("nat") int pumpNumber) implements OutputEvent {}

    /** PUMP_CONTROL_REPAIRED_ACKNOWLEDGEMENT(n). */
    record PumpControlRepairedAcknowledgement(@RoboChartType("nat") int pumpNumber) implements OutputEvent {}

    /** LEVEL_REPAIRED_ACKNOWLEDGEMENT. */
    record LevelRepairedAcknowledgement() implements OutputEvent {}

    /** STEAM_REPAIRED_ACKNOWLEDGEMENT. */
    record SteamRepairedAcknowledgement() implements OutputEvent {}
}
